package io.github.googlielmo.minichat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatServer {

    public static final int PORT = 10000;

    private static final Logger logger = Logger.getLogger("ChatServer");

    Collection<ClientHandler> clientHandlers = new ConcurrentLinkedQueue<>();

    public void runServer() {
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            serverSocket = new ServerSocket(PORT);
            logger.info("Listening to port " + PORT);
        } catch (IOException e) {
            // port not available, quit
            logger.log(Level.SEVERE, "Cannot listen to port " + PORT, e);
            return;
        }
        while (true) {
            try {
                socket = serverSocket.accept();
                logger.info("new client connected from " + socket.getInetAddress() + ":" + socket.getPort());
                final ClientHandler clientHandler = new ClientHandler(this, socket);
                clientHandlers.add(clientHandler);
                new Thread(clientHandler).start();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Client connection error", e);
            }
        }
    }

    public void dispatchMessage(ClientHandler originator, String line) {
        logger.fine("Dispatching message from " + originator);
        for (ClientHandler client : clientHandlers) {
            if (client != originator) {
                try {
                    logger.fine("Dispatching to " + client);
                    client.sendMessage(line);
                } catch (IOException e) {
                   logger.log(Level.WARNING, "Cannot send message to " + client + ": removing it", e);
                   clientHandlers.remove(client);
                }
            }
        }
    }

    public static void main(String args[]) {
        new ChatServer().runServer();
    }
}