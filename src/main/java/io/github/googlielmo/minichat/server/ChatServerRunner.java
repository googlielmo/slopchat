package io.github.googlielmo.minichat.server;

/**
 * Chat server main class
 * Invoke main with arguments: [port]
 */
public class ChatServerRunner {

    private int port = 10000;

    private ChatServerRunner parseOptions(String[] args) {
        if (args.length > 1) {
            System.out.println("Arguments: [port]");
            System.exit(1);
        }
        if (args.length >= 1) {
            port = Integer.valueOf(args[0]);
        }
        return this;
    }

    private void execute() {
        new ChatServer(port).serve();
    }

    public static void main(String args[]) {
        new ChatServerRunner()
                .parseOptions(args)
                .execute();
    }
}
