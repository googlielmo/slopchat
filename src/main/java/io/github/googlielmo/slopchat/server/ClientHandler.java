package io.github.googlielmo.slopchat.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Per-client receive/dispatch loop and send method.
 * To be run as a separate thread.
 */
class ClientHandler implements Runnable {

    private static Logger logger = Logger.getLogger("ClientHandler");

    protected Socket socket;

    BufferedReader socketReader;

    PrintWriter socketWriter;

    private ChatServer server;

    private String name;

    public ClientHandler(ChatServer server, Socket clientSocket) {
        this.server = server;
        this.socket = clientSocket;
        this.name = "ClientHandler[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]";
    }

    /**
     * Receive/dispatch messages from this client
     */
    public void run() {
        try {
            Thread.currentThread().setName(name);
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(socket.getOutputStream());

            while (true) {
                // receive message from network
                String message = socketReader.readLine();
                logger.fine("Received : " + message);
                if (message == null) {
                    logger.fine("`null` received, terminating client handler thread");
                    return;
                }
                // dispatch message to the other clients
                server.dispatchMessage(this, message);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error, disconnecting client", e);
        } finally {
            server.removeClientHandler(this);
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing socket", e);
            }
        }
    }

    /**
     * Send a message to this client
     * @param message the message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        logger.fine("sendMessage: " + message);
        socketWriter.println(message);
        socketWriter.flush();
    }

    public String toString() {
        return name;
    }
}