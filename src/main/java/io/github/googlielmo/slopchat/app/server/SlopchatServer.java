package io.github.googlielmo.slopchat.app.server;

import io.github.googlielmo.slopchat.server.ChatServer;

/**
 * Chat server executable
 * Invoke main with arguments: [port]
 * Default: 10000
 */
public class SlopchatServer {

    private int port = 10000;

    private SlopchatServer parseOptions(String[] args) {
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
        new SlopchatServer()
                .parseOptions(args)
                .execute();
    }
}
