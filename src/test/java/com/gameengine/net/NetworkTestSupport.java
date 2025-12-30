package com.gameengine.net;

import org.junit.Assume;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SocketChannel;

final class NetworkTestSupport {
    private NetworkTestSupport() {}

    static void assumeLoopbackSocketsAllowed() {
        if (Boolean.getBoolean("com.gameengine.net.tests.force")) {
            return;
        }
        Assume.assumeTrue("Sandbox does not allow loopback sockets", canBindAndConnectLoopback());
    }

    private static boolean canBindAndConnectLoopback() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            int port = serverSocket.getLocalPort();
            try (SocketChannel channel = SocketChannel.open()) {
                channel.configureBlocking(true);
                channel.connect(new InetSocketAddress("127.0.0.1", port));
            }
            return true;
        } catch (SecurityException | IOException e) {
            return false;
        }
    }
}

