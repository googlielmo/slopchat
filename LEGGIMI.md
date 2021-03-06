👉 Documentation in [English](README.md)

# slopchat

Un esercizio di socket programming in Java: implementiamo un sistema di chat senza fronzoli, compatibile con telnet!

Questo progetto include una implementazione base del protocollo SLOP in Java.

## Il protocollo SLOP

SLOP sta per Simple Line Oriented Protocol (Semplice Protocollo Orientato alle Linee,
o SPOL in Italiano se preferisci).

Un server slopchat riceve multiple connessioni su una data porta TCP (la porta di default è 10000).

Un client slopchat si connette a un server e può così scambiare messaggi SLOP con altri client.

Ciascun messaggio della chat è rappresentato come una stringa di una sola linea, ovvero una stringa
di lunghezza variabile terminata da un carattere LF (linefeed, ASCII 10 o `'\n'`).

Quando un client invia un messaggio a un server, il server manda una copia del messaggio a tutti gli
altri client connessi.

Al client mittente non viene mai mandata una copia del messaggio.

Nessun tentativo di ritrasmissione viene fatto in caso di fallimento della comunicazione TCP,
e la storia dei messaggi non viene mantenuta sul server (eccetto indirettamente nei log,
se sono configurati opportunamente a tale scopo).
I messaggi inviati prima che un client si sia connesso non possono essere ricevuti dal client.
Allo stesso modo, i messaggi inviati dopo che un client si è disconnesso non sono disponibili
in caso di futura riconnessione.

Un messaggio che viene ricevuto non porta con sé alcuna informazione sul mittente.
Questo aspetto viene lasciato da implementare a protocolli di più alto livello
(o alla interazione umana).

## Come usare slopchat per chattare con i tuoi amici della LAN

Vediamo come lanciare un server e un client su una o più macchine nella tua rete locale. (O da qualunque altra parte!)

### Lanciare un server

Accertati di avere Java (v. >= 1.8) e Maven istallati sulla tua macchina.

Su Linux/Unix/Mac puoi usare lo script `startServer.sh`.

```shell script
> cd slopchat
> ./startServer.sh 10000
```
Questo comando compila il codice, se necessario, e lancia un server sulla porta 10000.
Ricorda di aprire il firewall e di dare al processo della JVM il permesso di ricevere connessioni in ingresso sulla
porta.

Su qualsiasi piattaforma (es. Windows) puoi compilare il codice manualmente con i comandi che seguono.

Per compilare:
```shell script
> mvn clean package
```

Per lanciare il server:
```shell script
> java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-1.0-SNAPSHOT.jar io.github.googlielmo.slopchat.app.server.SlopchatServer 10000
```

Puoi modificare il file `logging.properties` fornito con il progetto sotto `src/main/resources/logging.properties` o
puntare a uno tuo.

### Lanciare un client

Sapevi che SLOP è compatibile con telnet? Per chattare puoi utilizzare il seguente comando.

```shell script
> telnet localhost 10000
```

Se invece vuoi provare l'originale client slopchat, su Linux/Unix/Mac puoi usare lo script `startClient.sh`.

```shell script
> cd slopchat
> ./startClient.sh localhost 10000
```
Questo comando compila il codice, se necessario, e lancia un client per connettersi a localhost porta 10000.

Su qualsiasi piattaforma (es. Windows) puoi lanciare il client con il comando seguente.

```shell script
> java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-1.0-SNAPSHOT.jar io.github.googlielmo.slopchat.app.client.Slopchat localhost 10000
```
## Usare il client e/o il server nel tuo codice

Puoi includere un server o client slopchat nel tuo progetto.

### Istanziare un server

Questo è tutto ciò che serve per iniziare a servire i client di slopchat:

```java
new ChatServer(port).serve();
```

Il metodo `serve` non è pensato per terminare, perciò probabilmente vorrai eseguirlo in un thread separato.

Questa implementazione usa un approccio thread-per-client, in cui viene creato un nuovo thread dedicato per
gestire ciascuna connessione per ricevere e distribuire i messaggi in ingresso.

#### Personalizzare il server

Vuoi personalizzare il server? Hai la possibilità di modificare o interpretare i messaggi, magari per implementare
un protocollo di più alto livello o aggiungere funzionalità, per es. chatroom, messaggi privati, nickname, ecc.
Per far questo basta implementare l'interfaccia `MessageProcessor` che ti consente di controllare tutti i messaggi in
ingresso e in uscita:

```java
    /**
     * Process an incoming message.
     * This method is invoked once per incoming message, before message dispatching.
     * @param message the original wire message
     * @param sender the sender ClientHandler
     * @return An optional string: the (possibly new) message to dispatch if present, or empty to ignore the message
     */
    Optional<String> processIncomingMessage(String message, ClientHandler sender);

    /**
     * Process an outgoing message.
     * This method is called during message dispatching, once for each connected client as recipient.
     * @param message the message to send, already processed by {@link #processIncomingMessage(String, ClientHandler)}.
     * @param sender the sender ClientHandler
     * @param recipient the recipient ClientHandler
     * @return An optional string: the (possibly new) message to send if present, or empty to suppress the sending
     */
    Optional<String> processSend(String message, ClientHandler sender, ClientHandler recipient);
```

Puoi preprocessare ogni messaggio che arriva al server mediante il metodo `processIncomingMessage`.
Il metodo restituisce una `Optional<String>` che se presente costituisce il messaggio da propagare agli altri client.
Se invece il messaggio ricevuto per qualsiasi motivo deve essere ignorato, ovvero non girato ad altri client,
si deve restituire `Optional.empty()`.

Analogamente, ciascun messaggio in uscita può essere processato implementando il metodo `processSend`, che viene
chiamato una volta per ciascun client collegato al server come destinatario.
Anche in questo caso il valore di ritorno è una `Optional<String>` che se presente costituisce il messaggio da inviare,
o in caso sia `empty` segnala al server di annullare un particolare invio.

L'implementazione di default di questi due metodi è molto semplice.
Il metodo `processIncomingMessage` non fa altro che restituire il messaggio in arrivo senza alcuna elaborazione.
Il metodo `processSend` invece controlla che il mittente sia diverso dal destinatario, e solo in questo caso
restituisce il messaggio inalterato per l'invio al destinatario.
Nel caso mittente e destinatario coincidano, l'invio viene soppresso.

Per istanziare un server con un `MessageProcessor` personalizzato, lo si deve passare al costruttore dedicato.

```java
new ChatServer(port, myMessageProcessor).serve();
```

### Implementare un client

Per far diventare la tua app un client slopchat devi istanziare un `ChatClient`:

```java
    ChatClient client = new ChatClient(serverName, port, eventHandler);
    client.connect();
```

In questo esempio `eventHandler` è la tua implementazione dell'interfaccia `ChatEventHandler`:

```
    /**
     * Server connection event
     */
    void onConnect();

    /**
     * Incoming message event
     * @param message the message
     */
    void onMessage(String message);

    /**
     * Server disconnection event
     */
    void onDisconnect();
```

Quando il tuo client riceve un messaggio, il metodo `onMessage` viene invocato sul tuo handler passandogli il messaggio
in ingresso.
I metodi `onConnect` e `onDisconnect` notificano al tuo handler rispettivamente gli eventi di connessione e
disconnessione.

Una volta stabilita una coneessione con il server, la tua app può inviare un messaggio invocando il metodo
`sendMessage`:

```java
    client.sendMessage(message);
```

Mettendo insieme tutti i pezzi del puzzle, il nostro client da console è implementato come segue, compresa la gestione
degli errori.

```java
    @Override
    public void onConnect() {
        System.out.println("Connected! ^C to quit");
    }

    @Override
    public void onMessage(String message) {
        // print incoming message to console
        System.out.println(message);
    }

    @Override
    public void onDisconnect() {
        System.out.println("Disconnected from server, bye");
        System.exit(0);
    }

    /**
     * Connect to server and handle console input
     * @throws IOException
     */
    private void execute() throws IOException {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        ChatClient client = new ChatClient(serverName, port, this);
        try {
            client.connect();
        } catch (IOException e) {
            System.out.println("Cannot connect to server, bye");
            System.exit(1);
        }

        while (true) {
            // read message from console
            String message = consoleReader.readLine();
            // send it to server
            client.sendMessage(message);
        }
    }
```

Questo è quanto. Divertiti con slopchat!

