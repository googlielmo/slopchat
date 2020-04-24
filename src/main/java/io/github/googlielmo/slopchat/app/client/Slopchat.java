package io.github.googlielmo.slopchat.app.client;

import io.github.googlielmo.slopchat.client.ChatClient;
import io.github.googlielmo.slopchat.client.ChatEventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Console client main class
 * Invoke main with arguments: [serverName] [port]
 * Defaults: "localhost" 10000
 */
public class Slopchat implements ChatEventHandler {

    private String serverName = "localhost";

    private int port = 10000;

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

    private Slopchat parseOptions(String[] args) {
        if (args.length > 2) {
            System.out.println("Arguments: [serverName] [port]");
            System.exit(1);
        }
        if (args.length >= 1) {
            serverName = args[0];
        }
        if (args.length >= 2) {
            port = Integer.valueOf(args[1]);
        }
        return this;
    }

    public static void main(String[] args) throws IOException {
        new Slopchat()
                .parseOptions(args)
                .execute();
    }
}
