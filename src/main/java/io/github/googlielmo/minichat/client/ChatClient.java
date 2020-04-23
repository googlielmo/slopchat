package io.github.googlielmo.minichat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient {

    private static final Logger logger = Logger.getLogger("ChatClient");

    private final ChatEventHandler eventHandler;

    private Socket socket = null;

    private BufferedReader socketReader = null;

    private PrintWriter socketWriter = null;

    private String serverName;

    private int port;

    private boolean connected;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ChatClient(String serverName, int port, ChatEventHandler eventHandler) {
        this.serverName = serverName;
        this.port = port;
        this.eventHandler = eventHandler;
    }

    public void connect() throws IOException {
        connected = false;
        try {
            socket = new Socket(serverName, port);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(socket.getOutputStream());
            startReceiver();
            connected = true;
            eventHandler.onConnect();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Cannot connect to server", e);
            throw e;
        }
    }

    public void sendMessage(String message) {
        socketWriter.println(message);
        socketWriter.flush();
    }

    public void disconnect() {
        executorService.shutdownNow();
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot close socket", e);
            }
        }
        connected = false;
        eventHandler.onDisconnect();
    }

    private void startReceiver() {
        final Runnable receiver = () -> {
            while (true) {
                if (Thread.interrupted()) {
                    logger.fine("Receiver interrupted");
                    break;
                }
                try {
                    String message = socketReader.readLine();
                    logger.fine("Received : " + message);
                    // send message event
                    eventHandler.onMessage(message);
                    if (message == null) {
                        logger.warning("`null` received, disconnecting");
                        disconnect();
                    }
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error receiving from server, terminating", e);
                    disconnect();
                }
            }
        };
        executorService.execute(receiver);
    }

    public boolean isConnected() {
        return connected;
    }
}