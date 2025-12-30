package com.gameengine.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 主 Reactor 线程
 * 负责接收客户端的连接（accept 事件）
 * 并将新连接分发给对应的子 Reactor 线程
 */
public class MainReactor implements Runnable {
    
    private final int port;
    private final SubReactor[] subReactors;
    private volatile boolean isRunning = false;
    private Thread thread;
    private int nextSubReactorIndex = 0; // 用于轮询分发连接
    
    /**
     * 构造函数
     * @param port 服务器端口
     * @param subReactors 子 Reactor 数组，用于分发连接
     */
    public MainReactor(int port, SubReactor[] subReactors) {
        this.port = port;
        this.subReactors = subReactors;
    }
    
    /**
     * 启动主 Reactor
     */
    public void start() {
        if (thread != null) {
            return;
        }
        isRunning = true;
        thread = new Thread(this, "MainReactor");
        thread.setDaemon(true);
        thread.start();
        System.out.println("[MainReactor] 主 Reactor 已启动，监听端口: " + port);
    }
    
    /**
     * 停止主 Reactor
     */
    public void stop() {
        isRunning = false;
        if (thread != null) {
            thread.interrupt();
        }
    }
    
    /**
     * 轮询选择下一个子 Reactor
     * 使用简单的轮询算法实现负载均衡
     * @return 下一个子 Reactor
     */
    private SubReactor getNextSubReactor() {
        SubReactor selected = subReactors[nextSubReactorIndex];
        nextSubReactorIndex = (nextSubReactorIndex + 1) % subReactors.length;
        return selected;
    }
    
    @Override
    public void run() {
        try (Selector selector = Selector.open();
             ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
            
            // 配置为非阻塞模式
            serverChannel.configureBlocking(false);
            // 绑定端口
            serverChannel.bind(new InetSocketAddress(port));
            // 注册 ACCEPT 事件
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            
            System.out.println("[MainReactor] 开始接受客户端连接...");
            
            while (isRunning) {
                // 最多阻塞 250 毫秒
                selector.select(250);
                
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    
                    if (!key.isValid()) {
                        continue;
                    }
                    
                    if (key.isAcceptable()) {
                        // 处理新连接
                        handleAccept(serverChannel);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("[MainReactor] 发生错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("[MainReactor] 主 Reactor 已停止");
        }
    }
    
    /**
     * 处理新连接
     * 接受连接后，将其分发给一个子 Reactor
     */
    private void handleAccept(ServerSocketChannel serverChannel) {
        try {
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                // 轮询选择一个子 Reactor
                SubReactor subReactor = getNextSubReactor();
                
                // 将新连接注册到选中的子 Reactor
                subReactor.registerChannel(clientChannel);
                
                System.out.println("[MainReactor] 新客户端连接已接受，分配给 SubReactor-" + 
                    subReactor.getId() + ": " + clientChannel.getRemoteAddress());
            }
        } catch (IOException e) {
            System.err.println("[MainReactor] 接受连接时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
