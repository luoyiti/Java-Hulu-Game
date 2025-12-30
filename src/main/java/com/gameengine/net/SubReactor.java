package com.gameengine.net;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 子 Reactor 线程
 * 负责处理客户端连接的读写事件（read/write）
 * 不负责连接管理，连接由主 Reactor 分发而来
 */
public class SubReactor implements Runnable {
    
    private final int id; // 子 Reactor 的唯一标识
    private final Selector selector;
    private volatile boolean isRunning = false;
    private Thread thread;
    private final CopyOnWriteArrayList<SocketChannel> connections = new CopyOnWriteArrayList<>();
    private Runnable connectionCountUpdater; // 连接数更新回调
    
    /**
     * 构造函数
     * @param id 子 Reactor 的唯一标识
     * @throws IOException 如果创建 Selector 失败
     */
    public SubReactor(int id) throws IOException {
        this.id = id;
        this.selector = Selector.open();
    }
    
    /**
     * 设置连接数更新回调
     * 当连接数变化时，会调用此回调来更新全局连接数
     */
    public void setConnectionCountUpdater(Runnable updater) {
        this.connectionCountUpdater = updater;
    }
    
    /**
     * 更新全局连接数
     */
    private void updateGlobalConnectionCount() {
        if (connectionCountUpdater != null) {
            connectionCountUpdater.run();
        }
    }
    
    /**
     * 获取子 Reactor 的 ID
     */
    public int getId() {
        return id;
    }
    
    /**
     * 启动子 Reactor
     */
    public void start() {
        if (thread != null) {
            return;
        }
        isRunning = true;
        thread = new Thread(this, "SubReactor-" + id);
        thread.setDaemon(true);
        thread.start();
        System.out.println("[SubReactor-" + id + "] 子 Reactor 已启动");
    }
    
    /**
     * 停止子 Reactor
     */
    public void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
        }
        // 关闭所有连接
        for (SocketChannel ch : connections) {
            try {
                ch.close();
            } catch (IOException ignored) {
            }
        }
        connections.clear();
    }
    
    /**
     * 注册一个新的客户端连接
     * 由主 Reactor 调用，将新接受的连接分发给此子 Reactor
     * @param channel 客户端 SocketChannel
     */
    public void registerChannel(SocketChannel channel) {
        try {
            channel.configureBlocking(false);
            // 注册读事件
            SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
            // 为每个连接创建独立的读缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            key.attach(new Reactor.ReadHandler(buffer));
            
            connections.add(channel);
            
            // 更新全局状态
            updateGlobalConnectionCount();
            try {
                SocketAddress addr = channel.getRemoteAddress();
                NetState.addClientAddress(addr);
                System.out.println("[SubReactor-" + id + "] 新连接已注册: " + addr);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // 唤醒 selector，以便立即处理新注册的连接
            selector.wakeup();
            
        } catch (IOException e) {
            System.err.println("[SubReactor-" + id + "] 注册连接失败: " + e.getMessage());
            e.printStackTrace();
            try {
                channel.close();
            } catch (IOException ignored) {
            }
        }
    }
    
    /**
     * 获取当前子 Reactor 管理的连接数
     */
    public int getConnectionCount() {
        return connections.size();
    }
    
    /**
     * 获取所有连接（用于广播）
     */
    public CopyOnWriteArrayList<SocketChannel> getConnections() {
        return connections;
    }
    
    @Override
    public void run() {
        System.out.println("[SubReactor-" + id + "] 开始处理读写事件...");
        
        while (isRunning) {
            try {
                // 最多阻塞 250 毫秒
                selector.select(250);
                
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    if (key.isReadable()) {
                        // 处理读事件
                        handleRead(key);
                    }
                }
                
                // 定期广播游戏状态（每 50 毫秒）
                try {
                    Reactor.WriteHandler.broadcast(connections);
                } catch (IOException e) {
                    System.err.println("[SubReactor-" + id + "] 广播时发生错误: " + e.getMessage());
                }
                
            } catch (IOException e) {
                System.err.println("[SubReactor-" + id + "] 处理事件时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 清理资源
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("[SubReactor-" + id + "] 子 Reactor 已停止");
    }
    
    /**
     * 处理读事件
     */
    private void handleRead(SelectionKey key) {
        try {
            Object attachment = key.attachment();
            if (attachment instanceof Reactor.ReadHandler) {
                Reactor.ReadHandler handler = (Reactor.ReadHandler) attachment;
                handler.handle(key);
                
                // 检查连接是否已关闭
                SocketChannel channel = (SocketChannel) key.channel();
                if (!channel.isOpen()) {
                    // 连接已关闭，从列表中移除
                    connections.remove(channel);
                    updateGlobalConnectionCount();
                }
            }
        } catch (IOException e) {
            System.err.println("[SubReactor-" + id + "] 处理读事件时发生错误: " + e.getMessage());
            
            // 发生错误，关闭连接
            try {
                SocketChannel channel = (SocketChannel) key.channel();
                if (channel != null) {
                    try {
                        NetState.removeClientAddress(channel.getRemoteAddress());
                    } catch (IOException ignored) {
                    }
                    channel.close();
                    connections.remove(channel);
                }
                key.cancel();
                updateGlobalConnectionCount();
            } catch (IOException ignored) {
            }
        }
    }
}