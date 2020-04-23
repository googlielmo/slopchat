package io.github.googlielmo.minichat.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
class ClientHandler implements Runnable {

    private static Logger logger = Logger.getLogger("ClientHandler");

    protected Socket socket;

    BufferedReader reader;

    DataOutputStream outputStream;

    private ChatServer server;

    private String name;

    public ClientHandler(ChatServer server, Socket clientSocket) {
        this.server = server;
        this.socket = clientSocket;
        this.name = "ClientHandler[" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]";
    }

    public void run() {
        try {
            Thread.currentThread().setName(name);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputStream = new DataOutputStream(socket.getOutputStream());

            while (true) {
                // receive message from network
                String message = reader.readLine();
                logger.fine("Received : " + message);
                if (message == null) {
                    logger.warning("`null` received, terminating client handler thread");
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

    public void sendMessage(String message) throws IOException {
        logger.fine("sendMessage: " + message);
        outputStream.writeBytes(message + "\n");
        outputStream.flush();
    }

    @Override
    public String toString() {
        return name;
    }
}