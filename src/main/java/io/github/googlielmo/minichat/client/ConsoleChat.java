package io.github.googlielmo.minichat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Console chat main class
 * Invoke main with arguments: [serverName] [port]
 */
public class ConsoleChat implements ChatEventHandler {

    private String serverName = "localhost";

    private int port = 10000;

    private BufferedReader consoleReader = null;

    private ChatClient client;

    public static void main(String[] args) throws IOException {
        new ConsoleChat()
                .parseOptions(args)
                .execute();
    }

    private void execute() throws IOException {
        consoleReader = new BufferedReader(new InputStreamReader(System.in));

        client = new ChatClient(serverName, port, this);
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

    private ConsoleChat parseOptions(String[] args) {
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
}
