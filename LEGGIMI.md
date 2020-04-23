👉 Documentation in [English](README.md)

# slopchat

Un esercizio di socket programming in Java.

Questo progetto include una semplice implementazione del protocollo SLOP in Java.

Un server slopchat riceve multiple connessioni su una data porta TCP (la porta di default è 10000).

Un client slopchat si connette a un server e può così scambiare messaggi SLOP con altri client.

## Il protocollo SLOP

SLOP sta per Simple Line Oriented Protocol (Semplice Protocollo Orientato alle Linee,
o SPOL in Italiano se preferite).

Ciascun messaggio della chat è rappresentato come una stringa di una sola linea, ovvero una stringa
di lunghezza variabile terminata da un carattere LF (linefeed, ASCII 10 o `'\n'`).

Quando un client invia un messaggio a un server, il server manda una copia del messaggio a tutti gli
altri client connessi.

Al client mittente non viene MAI mandata una copia del messaggio.

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
> java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-1.0-SNAPSHOT.jar io.github.googlielmo.slopchat.server.ChatServerRunner 10000
```

Puoi modificare il file `logging.properties` fornito con il progetto sotto `src/main/resources/logging.properties` o
puntare a uno tuo.

### Lanciare un client

Su Linux/Unix/Mac puoi usare lo script `startClient.sh`.

```shell script
> cd slopchat
> ./startClient.sh localhost 10000
```
Questo comando compila il codice, se necessario, e lancia un client per connettersi a localhost porta 10000.

Su qualsiasi piattaforma (es. Windows) puoi lanciare il client con il comando seguente.

```shell script
> java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-1.0-SNAPSHOT.jar io.github.googlielmo.slopchat.client.ConsoleChat localhost 10000
```
## Usare il client e/o il server nel tuo codice

TBD
