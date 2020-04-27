package io.github.googlielmo.slopchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SLOP (Simple Line Oriented Protocol) chat server
 */
public class ChatServer {

    public static final int PORT = 10000;

    private static final Logger logger = Logger.getLogger("ChatServer");

    private final MessageProcessor messageProcessor;

    private final Collection<ClientHandler> clientHandlers = new ConcurrentLinkedQueue<>();

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    private final int port;

    /**
     * Create a default ChatServer listening on the default PORT (10000)
     */
    public ChatServer() {
        this(PORT, null);
    }

    /**
     * @param port TCP port number for listening
     */
    public ChatServer(int port) {
        this(port, null);
    }

    /**
     * @param port             TCP port number for listening
     * @param messageProcessor a {@link MessageProcessor} or <code>null</code> for the default behaviour.
     */
    public ChatServer(int port, MessageProcessor messageProcessor) {
        this.port = port;
        this.messageProcessor = messageProcessor != null ? messageProcessor : new MessageProcessor() {
            @Override
            public Optional<String> processIncomingMessage(String message, ClientHandler sender) {
                // message is dispatched unchanged
                return Optional.of(message);
            }

            @Override
            public Optional<String> processSend(String message, ClientHandler sender, ClientHandler recipient) {
                if (sender != recipient) {
                    return Optional.of(message);
                } else {
                    // don't send a copy of the message back to its sender
                    return Optional.empty();
                }
            }
        };
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
     * Remove the ClientHandler for a disconnected client
     *
     * @param clientHandler the client to remove
     */
    void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    /**
     * Dispatch a message to connected clients.
     * The standard MessageProcessor sends a copy of the message, unchanged, to all
     * clients except the sender.
     *
     * @param message the message
     * @param sender  the client that is sending the message
     */
    void dispatchMessage(String message, ClientHandler sender) {
        logger.fine("Processing message from " + sender);
        messageProcessor
                .processIncomingMessage(message, sender)
                .ifPresent(processedMessage -> dispatchAll(sender, processedMessage));
    }

    private void dispatchAll(ClientHandler sender, String message) {
        logger.fine("Dispatching message from " + sender);
        for (ClientHandler client : clientHandlers) {
            messageProcessor
                    .processSend(message, sender, client)
                    .ifPresent(processedMessage -> dispatchOne(processedMessage, client));
        }
    }

    private void dispatchOne(String message, ClientHandler client) {
        try {
            logger.fine("Sending to " + client);
            client.sendMessage(message);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Cannot send message to " + client + ": removing it", e);
            removeClientHandler(client);
        }
    }
}