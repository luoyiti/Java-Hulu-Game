package com.gameengine.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.gameengine.game.InputRecord;
import com.gameengine.game.Record;

import com.gameengine.input.InputManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * NIO 客户端
 * 负责连接服务器并发送玩家输入
 */
public class NioClient {
    private SocketChannel channel;
    private volatile boolean inputLoopStarted = false;
    private volatile boolean stateLoopStarted = false;
    private volatile boolean connected = false;
    private String myAddressId;
    private static final Gson gson = new Gson();

    public boolean connect(String host, int port) {
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));
            myAddressId = channel.getLocalAddress().toString();
            connected = true;
            System.out.println("[Client] Connected to server: " + channel.getRemoteAddress());
            return true;
        } catch (IOException e) {
            System.err.println("[Client] Connection failed: " + e.getMessage());
            return false;
        }
    }

    public boolean join(String name) {
        if (channel == null || !connected) return false;

        try {
            ByteBuffer out = ByteBuffer.wrap(("JOIN:" + name + "\n").getBytes());
            while (out.hasRemaining()) channel.write(out);

            ByteBuffer in = ByteBuffer.allocate(4096);
            StringBuilder response = new StringBuilder();
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < 3000) {
                in.clear();
                int n = channel.read(in);
                if (n > 0) {
                    response.append(new String(in.array(), 0, n));
                    if (response.toString().contains("JOIN-ACK")) {
                        System.out.println("[Client] Joined successfully");
                        return true;
                    }
                }
                try { Thread.sleep(50); } catch (InterruptedException ignored) {}
            }

            System.err.println("[Client] Join timeout");
            return false;

        } catch (IOException e) {
            System.err.println("[Client] Join failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * 启动输入发送循环
     * 持续读取玩家输入并发送给服务器
     */
    public void startInputLoop(final InputManager input) {
        if (channel == null || inputLoopStarted || !connected) return;
        inputLoopStarted = true;

        Thread t = new Thread(() -> {
            try {
                while (channel.isOpen() && connected) {
                    float dx = 0, dy = 0;
                    
                    // 读取方向键输入
                    if (input.isKeyPressed(87) || input.isKeyPressed(265)) dy -= 1; // W / Up
                    if (input.isKeyPressed(83) || input.isKeyPressed(264)) dy += 1; // S / Down
                    if (input.isKeyPressed(65) || input.isKeyPressed(263)) dx -= 1; // A / Left
                    if (input.isKeyPressed(68) || input.isKeyPressed(262)) dx += 1; // D / Right
                    
                    float speed = 200f;
                    float magnitude = (float) Math.hypot(dx, dy);
                    float vx = magnitude > 0 ? (dx / magnitude) * speed : 0;
                    float vy = magnitude > 0 ? (dy / magnitude) * speed : 0;

                    // 创建输入记录
                    InputRecord inputRecord = new InputRecord(myAddressId, vx, vy);
                    String json = gson.toJson(inputRecord) + "\n";

                    ByteBuffer out = ByteBuffer.wrap(json.getBytes());
                    while (out.hasRemaining()) channel.write(out);
                    
                    try { Thread.sleep(16); } catch (InterruptedException ignored) {} // ~60fps
                }
            } catch (Exception e) {
                System.err.println("[Client] Input loop error: " + e.getMessage());
            }
        }, "client-input-loop");
        t.setDaemon(true);
        t.start();
    }

    public void disconnect() {
        connected = false;
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException ignored) {}
        }
    }

    public boolean isConnected() {
        return connected && channel != null && channel.isOpen();
    }

    public String getMyAddressId() {
        return myAddressId;
    }

    public void startStateReceiveLoop() {
        if (channel == null || stateLoopStarted || !connected) return;
        stateLoopStarted = true;

        Thread t = new Thread(() -> {
            ByteBuffer in = ByteBuffer.allocate(4096);
            StringBuilder sb = new StringBuilder();
            try {
                while (channel.isOpen() && connected) {
                    in.clear();
                    int n = channel.read(in);
                    if (n <= 0) {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException ignored) {
                        }
                        continue;
                    }
                    sb.append(new String(in.array(), 0, n));
                    int idx;
                    while ((idx = sb.indexOf("\n")) >= 0) {
                        String line = sb.substring(0, idx).trim();
                        sb.delete(0, idx + 1);
                        if (line.isEmpty()) continue;
                        try {
                            Record record = gson.fromJson(line, Record.class);
                            if (record != null) {
                                // 保存最新快照
                                NetState.currentRecords = record;
                                // 推入插值缓冲区
                                NetworkBuffer.pushRecord(record);
                            }
                        } catch (Exception e) {
                            System.err.println("[Client] Failed to parse state: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("[Client] State receive loop error: " + e.getMessage());
            }
        }, "client-state-loop");
        t.setDaemon(true);
        t.start();
    }
}
