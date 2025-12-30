package com.gameengine.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.gameengine.game.InputRecord;
import com.google.gson.Gson;

/**
 * 服务器类
 * 负责处理所有的游戏逻辑
 * 接受客户端的输入
 * 并且向客户端发送渲染信息
 */
public class NioServer implements Runnable {

    private final int port;
    private boolean isRunning;
    private Thread thread;
    private final List<SocketChannel> conns = new ArrayList<>();
    private static Gson gson = new Gson();

    private interface Handler {
        void handle(SelectionKey key) throws IOException;
    }

    private class AcceptHandler implements Handler {
        private final Selector selector;
        private final ServerSocketChannel serverChannel;
        private final ByteBuffer buffer;

        AcceptHandler(Selector selector, ServerSocketChannel serverChannel, ByteBuffer buffer) {
            this.selector = selector;
            this.serverChannel = serverChannel;
            this.buffer = buffer;
        }

        @Override
        public void handle(SelectionKey key) throws IOException {
            if (!key.isAcceptable()) {
                return;
            }
            SocketChannel ch = serverChannel.accept(); // 接受连接并得到一个新的 channel
            if (ch != null) {
                ch.configureBlocking(false);
                SelectionKey clientKey = ch.register(selector, SelectionKey.OP_READ); // 关注它的读时间
                clientKey.attach(new ReadHandler(buffer));
                conns.add(ch);

                NetState.setClientCount(conns.size()); // 更新缓存的连接数

                try {
                    // 将新连接的地址添加到 NetState
                    SocketAddress addr = ch.getRemoteAddress();
                    NetState.addClientAddress(addr);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private class ReadHandler implements Handler {
        private final ByteBuffer buffer;

        ReadHandler(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void handle(SelectionKey key) throws IOException {
            if (!key.isReadable()) {
                return;
            }
            SocketChannel ch = (SocketChannel) key.channel(); // 得到原先的 channel
            buffer.clear();
            int n = ch.read(buffer); // 从通道中读取数据到缓冲区

            if (n <= 0) { // 连接异常
                key.cancel();
                conns.remove(ch);
                NetState.setClientCount(conns.size()); // 更新缓存的连接数
                try {
                    // 移除保存的地址（在关闭channel之前获取地址）
                    java.net.SocketAddress addr = ch.getRemoteAddress();
                    NetState.removeClientAddress(addr);
                } catch (IOException e) {
                    // ignore - channel may already be closed
                }
                try {
                    ch.close();
                } catch (IOException e) {
                    // ignore
                }
                return;
            }

            buffer.flip(); // 缓冲区从读模式转换为写模式

            // 将读到的内容转换为字符串
            String data = new String(buffer.array(), 0, buffer.limit());
            // 按行分割处理多条消息
            String[] lines = data.split("\n");
            for (String s : lines) {
                s = s.trim();
                if (s.isEmpty()) {
                    continue;
                }
                if (s.startsWith("JOIN:")) {
                    // 构造用于写的缓冲区，并保证完整写出
                    // 对接客户端的加入请求
                    ByteBuffer out = ByteBuffer.wrap("JOIN-ACK\n".getBytes());
                    while (out.hasRemaining())
                        ch.write(out);
                } else if (s.contains("INPUT")) {
                    // 获取客户端玩家的输入
                    String payload = s;
                    if (s.startsWith("INPUT:")) {
                        payload = s.substring("INPUT:".length()).trim();
                    }
                    try {
                        InputRecord inputRecord = gson.fromJson(payload, InputRecord.class);
                        if (inputRecord != null) {
                            NetState.updateClientInput(inputRecord);
                        }
                    } catch (RuntimeException e) {
                        // 忽略格式错误的输入，避免影响后续连接
                    }
                }
                // 未识别的消息直接忽略，不影响后续处理
            }
        }
    }

    public NioServer(int port) {
        this.port = port;
    }

    public void start() {
        if (thread != null)
            return;
        isRunning = true;
        thread = new Thread(this, "Nio Server");
        thread.setDaemon(true);
        thread.start();
        System.out.println("[Server] Started on port " + port);
    }

    public void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void run() {
        try (Selector selector = Selector.open(); // 多路复用
                ServerSocketChannel ssc = ServerSocketChannel.open() // 打开服务端 NIO 接口
        ) {
            ssc.configureBlocking(false); // 开启非阻塞模式
            ssc.bind(new InetSocketAddress(port)); // 绑定端口
            SelectionKey serverKey = ssc.register(selector, SelectionKey.OP_ACCEPT); // 将 ServerSocketChannel 注册到 Selector 并监听 OP_ACCEPT 事件

            ByteBuffer buffer = ByteBuffer.allocate(1024); // 分配一个缓冲区
            serverKey.attach(new AcceptHandler(selector, ssc, buffer));
            long lastBoradcastTime = System.currentTimeMillis();
            // 记载上一次向客户端广播状态的时间戳，以实现每50毫秒一次广播

            while (isRunning) {
                selector.select(250); // 最多阻塞250毫秒
                // 返回这次 select 操作中所有发生事件的 SelectionKey 集合
                // 并将其转换为可迭代对象
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isValid())
                        continue;

                    Object attachment = key.attachment();
                    if (attachment instanceof Handler) {
                        Handler handler = (Handler) attachment;
                        handler.handle(key);
                    }
                }
                // 在这里，将服务端所处理的所有渲染运算都广播给客户端
                long now = System.currentTimeMillis();
                if (now - lastBoradcastTime >= 50) {
                    lastBoradcastTime = now;
                    String json = gson.toJson(NetState.currentRecords);
                    if (json != null && !json.isEmpty()) {
                        // 追加换行符，方便客户端按行解析
                        json = json + "\n";
                        ByteBuffer out = ByteBuffer.wrap(json.getBytes());
                        for (int i = conns.size() - 1; i >= 0; i--) {
                            SocketChannel ch = conns.get(i);
                            if (!ch.isOpen()) {
                                conns.remove(i);
                                continue;
                            }
                            out.rewind();
                            try {
                                while (out.hasRemaining())
                                    ch.write(out);
                            } catch (IOException ignored) {
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
