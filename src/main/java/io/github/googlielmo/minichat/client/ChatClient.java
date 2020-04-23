package io.github.googlielmo.minichat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatClient {

    public static final int PORT = 10000;

    private static final Logger logger = Logger.getLogger("ChatClient");

    Socket socket = null;
    BufferedReader consoleReader = null;
    BufferedReader socketReader = null;
    PrintWriter socketWriter = null;
    private String serverName;

    public ChatClient() {
        serverName = "localhost";
    }

    public static void main(String[] args) throws IOException {
        new ChatClient()
                .withConfig(args)
                .runClient();
    }

    private ChatClient withConfig(String[] args) {
        if (args.length == 1) {
            serverName = args[0];
        }
        return this;
    }

    public void runClient() throws IOException {
        try {
            socket = new Socket(serverName, PORT);
            consoleReader = new BufferedReader(new InputStreamReader(System.in));
            socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            // cannot connect to server, quit
            logger.log(Level.SEVERE, "Cannot connect to server", e);
            return;
        }

        final Thread receiver = startReceiver();

        System.out.println("Connected! ^C to quit");
        try {
            while (true) {
                // read message from console
                String message = consoleReader.readLine();
                socketWriter.println(message);
                socketWriter.flush();
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error sending to server", e);
        } finally {
            receiver.interrupt();
            socketReader.close();
            socketWriter.close();
            consoleReader.close();
            socket.close();
        }
    }

    private Thread startReceiver() {
        final Thread receiver = new Thread(() -> {
            while (true) {
                if (Thread.interrupted()) {
                    logger.fine("Receiving thread interrupted");
                    break;
                }
                try {
                    String message = socketReader.readLine();
                    logger.fine("Received : " + message);
                    // print message to console
                    System.out.println(message);
                    if (message == null) {
                        logger.warning("`null` received, quitting");
                        return;
                    }
                } catch (IOException e) {
                    // quit receiving thread
                    logger.log(Level.SEVERE, "Error receiving from server", e);
                    return;
                }
            }
        }
        );
        receiver.start();
        return receiver;
    }
}