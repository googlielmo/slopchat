package io.github.googlielmo.slopchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SLOP (Simple Line-Oriented Protocol) chat server
 */
public class ChatServer {

    private static final Logger logger = Logger.getLogger("ChatServer");

    private Collection<ClientHandler> clientHandlers = new ConcurrentLinkedQueue<>();

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private int port;

    public ChatServer() {
        this(10000);
    }

    public ChatServer(int port) {
        this.port = port;
    }

    /**
     * Chat server main loop:
     * - accept new connections
     * - start a new thread per client
     */
    public void serve() {
        ServerSocket serverSocket;

        try {
            serverSocket = new ServerSocket(port);
            logger.info("Listening to port " + port);
        } catch (IOException e) {
            // port not available, quit
            logger.log(Level.SEVERE, "Cannot listen to port " + port, e);
            return;
        }
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                logger.info("new client connected from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
                final ClientHandler clientHandler = new ClientHandler(this, socket);
                clientHandlers.add(clientHandler);
                executorService.execute(clientHandler);
            } catch (IOException e) {
                logger.log(Level.WARNING, "Client connection error", e);
            }
        }
    }

    /**
     * Dispatch a message to all connected clients, except the sender
     *
     * @param sender  the client that is sending the message
     * @param message the message
     */
    public void dispatchMessage(ClientHandler sender, String message) {
        logger.fine("Dispatching message from " + sender);
        for (ClientHandler client : clientHandlers) {
            if (client != sender) {
                try {
                    logger.fine("Dispatching to " + client);
                    client.sendMessage(message);
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Cannot send message to " + client + ": removing it", e);
                    removeClientHandler(client);
                }
            }
        }
    }

    /**
     * Remove the ClientHandler for a disconnected client
     *
     * @param clientHandler the client to remove
     */
    public void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

}