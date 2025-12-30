package com.gameengine.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

import com.gameengine.game.InputRecord;
import com.google.gson.Gson;

/**
 * Reactor 基类
 * 提供通用的 Handler 实现
 * 主 Reactor 和子 Reactor 将分别继承或使用这些 Handler
 */
public class Reactor {
    
    protected static Gson gson = new Gson();
    
    /**
     * Handler 接口
     * 用于处理不同类型的事件
     */
    protected interface Handler {
        void handle(SelectionKey key) throws IOException;
    }
    
    /**
     * 读取处理器
     * 处理客户端发送的数据
     */
    protected static class ReadHandler implements Handler {
        private final ByteBuffer buffer;

        ReadHandler(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void handle(SelectionKey key) throws IOException {
            if (!key.isReadable()) {
                return;
            }
            SocketChannel ch = (SocketChannel) key.channel();
            buffer.clear();
            int n = ch.read(buffer);

            if (n <= 0) { // 连接异常或关闭
                key.cancel();
                ch.close();
                try {
                    NetState.removeClientAddress(ch.getRemoteAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            buffer.flip();
            String s = new String(buffer.array(), 0, buffer.limit());
            
            if (s.startsWith("JOIN:")) {
                // 处理客户端加入请求
                ByteBuffer out = ByteBuffer.wrap("JOIN-ACK\n".getBytes());
                while (out.hasRemaining())
                    ch.write(out);
            } else if (s.contains("INPUT")) {
                // 处理客户端输入
                s = s.trim();
                InputRecord inputRecord = gson.fromJson(s, InputRecord.class);
                if (inputRecord != null) {
                    NetState.updateClientInput(inputRecord);
                }
            }
        }
    }
    
    /**
     * 写入处理器
     * 用于广播游戏状态给所有客户端
     */
    protected static class WriteHandler {
        private static long lastBroadcastTime = System.currentTimeMillis();
        
        /**
         * 广播游戏状态给指定的连接列表
         * @param connections 要广播的连接列表
         */
        public static void broadcast(List<SocketChannel> connections) throws IOException {
            long now = System.currentTimeMillis();
            if (now - lastBroadcastTime >= 50) { // 每50毫秒广播一次
                lastBroadcastTime = now;
                String json = gson.toJson(NetState.currentRecords);
                if (json != null && !json.isEmpty()) {
                    json = json + "\n";
                    ByteBuffer out = ByteBuffer.wrap(json.getBytes());
                    
                    // 遍历所有连接并发送数据
                    for (int i = connections.size() - 1; i >= 0; i--) {
                        SocketChannel ch = connections.get(i);
                        if (!ch.isOpen()) {
                            connections.remove(i);
                            continue;
                        }
                        out.rewind();
                        try {
                            while (out.hasRemaining())
                                ch.write(out);
                        } catch (IOException ignored) {
                            // 写入失败，移除连接
                            connections.remove(i);
                        }
                    }
                }
            }
        }
    }
}
