👉 Documentazione in [Italiano](LEGGIMI.md)

# slopchat

An exercise in socket programming in the Java programming language: Implementing a no-frills online chat system!

This project includes a basic implementation of the SLOP protocol in Java.

## The SLOP protocol

SLOP stands for Simple Line Oriented Protocol.

A slopchat server listens to multiple connections on a given TCP port.

A slopchat client connects to a server and can exchange SLOP messages with other clients.

Each chat message is represented as a single line string, i.e. a string of variable length terminated
by a LF character.

When a client sends a message to a server, the server sends a copy of the message to all the other
connected clients.

A copy of the message is never sent back to the sender client.

No attempt is made to re-send a failed communication, and no history is kept on the server:
Messages sent before a client has connected cannot be received by the client.
Likewise, messages sent after a client has disconnected are not available for a future reconnection.

A received message has no sender information. This is left to higher-level protocols
(or human interaction) to implement.

## How to use slopchat to chat with your LAN friends

Let's see how to start a server and a client on one or more machines in your Local Area Network.

### Starting a server

Make sure you have Java (v. >= 1.8) and Maven installed on your development machine.

On Linux/Unix/Mac please use the provided `startServer.sh` script.

```shell script
> cd slopchat
> ./startServer.sh 10000
```
The command above will compile the code and start a server on port 10000.
Remember to open your firewall and give the JVM process the permission to receive incoming connections to the port.

On any platform  (e.g., Windows) you can compile and run the code manually with the following commands.

To compile the code:
```shell script
> mvn clean package
```

To start the server:
```shell script
> java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-1.0-SNAPSHOT.jar io.github.googlielmo.slopchat.server.ChatServerRunner 10000
```

You can edit the sample `logging.properties` file in the project under `src/main/resources/logging.properties` or
provide your own.

### Starting a client

On Linux/Unix/Mac please use the provided `startClient.sh` script.

```shell script
> cd slopchat
> ./startClient.sh localhost 10000
```
The command above will compile the code and start a client for the service on localhost port 10000.

On any platform (e.g., Windows) you can start the client manually with the following command. 

```shell script
> java -Djava.util.logging.config.file=./src/main/resources/logging.properties -cp ./target/slopchat-1.0-SNAPSHOT.jar io.github.googlielmo.slopchat.client.ConsoleChat localhost 10000
```
## Using the client and/or the server from your own code

You can embed a slopchat server or client in your own project.

### Instantiating a server

This is all you need to start serving slopchat clients:

```java
new ChatServer(port).serve();
```

The `serve()` method is not meant to return, therefore you may want to execute it in a separate thread.

This implementation uses a thread-per-client approach, where a new dedicated thread is created
to handle each connection for receiving and dispatching incoming messages.

### Implementing a client

To turn your app into a slopchat client you instantiate a `ChatClient`:

```java
    ChatClient client = new ChatClient(serverName, port, eventHandler);
    client.connect();
```

In the snippet above `eventHandler` is your own implementation of the `ChatEventHandler` interface:

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

When your client receives a message the `onMessage` method is invoked on your handler to pass it the incoming message.
The `onConnect` and `onDisconnect` methods notify your handler about connection and disconnection events, respectively.

After a successful connection with the server, your app can send a message by invoking the `sendMessage` method:

```java
    client.sendMessage(message);
```

Putting it all together, our console-based client looks like the following, error handling and all.

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

That's it. Have fun with slopchat!
