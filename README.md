👉 Documentazione in [Italiano](LEGGIMI.md)

# slopchat

An exercise in socket programming in the Java programming language.

This project includes a very simple chat implementation of the SLOP protocol in Java.

A slopchat server listens to multiple connections on a given TCP port.

A slopchat client connects to a server and can exchange SLOP messages with other clients.

## The SLOP protocol

SLOP stands for Simple Line Oriented Protocol.

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

TBD
