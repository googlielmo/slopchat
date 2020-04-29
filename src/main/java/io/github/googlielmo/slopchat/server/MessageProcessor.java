package io.github.googlielmo.slopchat.server;

import java.util.Optional;

/**
 * A message processor. Extra server behaviour (e.g., a higher level protocol)
 * can be plugged in by providing a suitable implementation.
 *
 * @see ChatServer#ChatServer(int, MessageProcessor)
 */
public interface MessageProcessor {

    /**
     * Process an incoming message.
     * This method is invoked once per incoming message, before message dispatching.
     * @param message the original wire message
     * @param sender the sender ClientHandler
     * @return An optional string: the (possibly new) message to dispatch if present, or empty to ignore the message
     */
    Optional<String> processIncomingMessage(String message, ClientHandler sender);

    /**
     * Process an outgoing message.
     * This method is called during message dispatching, once for each connected client as recipient.
     * @param message the message to send, already processed by {@link #processIncomingMessage(String, ClientHandler)}.
     * @param sender the sender ClientHandler
     * @param recipient the recipient ClientHandler
     * @return An optional string: the (possibly new) message to send if present, or empty to suppress the sending
     */
    Optional<String> processSend(String message, ClientHandler sender, ClientHandler recipient);
}
