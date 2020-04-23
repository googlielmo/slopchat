package io.github.googlielmo.minichat.client;

/**
 * A handler for chat events
 */
public interface ChatEventHandler {

    /**
     * Server connection event
     */
    void onConnect();

    /**
     * Incoming message event
     * @param message the message
     */
    void onMessage(String message);

    /**
     * Server disconnection event
     */
    void onDisconnect();

}
