package io.github.googlielmo.slopchat.client;

import io.github.googlielmo.slopchat.server.ChatServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ChatClient
 */
class ChatClientTest {

    public static final int TEST_PORT = 23456;

    static ChatServer server;

    static Thread serverThread;

    static ChatClient client1;

    static int countConnect = 0;

    static int countMessage = 0;

    static int countDisconnect = 0;

    static String lastMessage = null;

    @BeforeAll
    static void setUp() {
        server = new ChatServer(TEST_PORT);
        serverThread = new Thread(() -> {
            server.serve();
        });
        serverThread.start();
    }

    @BeforeEach
    void setUpTest() {
        countConnect = 0;
        countMessage = 0;
        countDisconnect = 0;
        lastMessage = null;
        client1 = new ChatClient("localhost", TEST_PORT, new ChatEventHandler() {
            @Override
            public void onConnect() {
                countConnect++;
            }

            @Override
            public void onMessage(String message) {
                countMessage++;
                lastMessage = message;
            }

            @Override
            public void onDisconnect() {
                countDisconnect++;
            }
        });
    }

    @AfterEach
    void tearDownTest() {
        if (client1 != null && client1.isConnected()) {
            client1.disconnect();
        }
    }

    @AfterAll
    static void tearDown() {
        serverThread.interrupt();
    }

    @Test
    void connect() throws IOException {
        // given

        // when
        client1.connect();

        // then
        assertTrue(countConnect == 1);
        assertTrue(client1.isConnected());
    }

    @Test
    void sendMessageWithOneClient() throws IOException {
        // given
        client1.connect();

        // when
        client1.sendMessage("test");

        // then
        // message is not delivered to self
        assertTrue(countMessage == 0);
        assertNull(lastMessage);
    }

    @Test
    void sendMessageWithTwoClients() throws IOException, InterruptedException {
        // given
        client1.connect();

        final ChatClient client2 = new ChatClient("localhost", TEST_PORT, new ChatEventHandler() {
            @Override
            public void onConnect() {}

            @Override
            public void onMessage(String message) {}

            @Override
            public void onDisconnect() {}
        });

        // when
        client2.connect();
        client2.sendMessage("test 2");

        // then
        // message is delivered to client1
        Thread.sleep(500);
        assertTrue(countMessage == 1);
        assertEquals("test 2", lastMessage);
    }
}
