package io.github.googlielmo.minichat.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static Logger logger = Logger.getLogger("ClientHandler");

    protected Socket socket;
    BufferedReader reader = null;
    DataOutputStream out = null;
    private ChatServer server;
    private String name;

    public ClientHandler(ChatServer server, Socket clientSocket) {
        this.server = server;
        this.socket = clientSocket;
        this.name = "ClientHandler[" + socket.getInetAddress() + ":" + socket.getPort() + "]";
    }

    public void run() {
        Thread.currentThread().setName(name);
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            return;
        }
        String message;
        while (true) {
            try {
                message = reader.readLine();
                logger.fine("Received : " + message);
                if (message == null) {
                    logger.warning("`null` received, quitting");
                    socket.close();
                    return;
                }
                server.dispatchMessage(this, message);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public void sendMessage(String message) throws IOException {
        logger.fine("sendMessage: " + message);
        out.writeBytes(message + "\n");
        out.flush();
    }

    @Override
    public String toString() {
        return name;
    }
}