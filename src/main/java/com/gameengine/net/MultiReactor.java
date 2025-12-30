package com.gameengine.net;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 多 Reactor 管理器
 * 负责创建和管理主 Reactor 和所有子 Reactor
 * 主 Reactor 负责接受连接，子 Reactor 负责处理读写事件
 * 这是暴露给游戏主程序的唯一入口类
 * 
 * 使用示例：
 * // 创建 MultiReactor，端口 8888，使用 4 个子 Reactor（建议设置为 CPU 核心数）
 * MultiReactor multiReactor = new MultiReactor(8888, 4);
 * 
 * // 启动服务器
 * multiReactor.start();
 * 
 * // 在游戏循环中，可以查询连接数
 * int connectionCount = multiReactor.getTotalConnectionCount();
 * 
 * // 停止服务器（通常在游戏结束时）
 * multiReactor.stop();
 * 
 * // 替换原有的 NioServer 使用方式：
 * // 旧代码：
 * //   NioServer server = new NioServer(8888);
 * //   server.start();
 * 
 * // 新代码：
 * //   MultiReactor server = new MultiReactor(8888, Runtime.getRuntime().availableProcessors());
 * //   server.start();
 */
public class MultiReactor {
    
    private final int port;
    private final int subReactorCount;
    
    // 主 Reactor：负责接受连接
    private MainReactor mainReactor;
    
    // 子 Reactor 数组：负责处理读写事件
    private SubReactor[] subReactors;
    
    // 运行状态
    private volatile boolean isRunning = false;
    
    /**
     * 构造函数
     * @param port 服务器监听端口
     * @param subReactorCount 子 Reactor 的数量（建议设置为 CPU 核心数）
     */
    public MultiReactor(int port, int subReactorCount) {
        if (subReactorCount <= 0) {
            throw new IllegalArgumentException("子 Reactor 数量必须大于 0");
        }
        this.port = port;
        this.subReactorCount = subReactorCount;
        
        // 创建子 Reactor 数组
        this.subReactors = new SubReactor[subReactorCount];
        
        try {
            // 初始化所有子 Reactor
            for (int i = 0; i < subReactorCount; i++) {
                subReactors[i] = new SubReactor(i);
                // 设置连接数更新回调，当连接变化时更新全局连接数
                subReactors[i].setConnectionCountUpdater(() -> {
                    NetState.setClientCount(getTotalConnectionCount());
                });
            }
            
            // 创建主 Reactor，传入子 Reactor 数组用于连接分发
            this.mainReactor = new MainReactor(port, subReactors);
            
            System.out.println("[MultiReactor] 初始化完成 - 端口: " + port + 
                ", 子 Reactor 数量: " + subReactorCount);
        } catch (IOException e) {
            System.err.println("[MultiReactor] 初始化失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("无法创建 MultiReactor", e);
        }
    }
    
    /**
     * 启动所有 Reactor
     * 先启动所有子 Reactor，再启动主 Reactor
     */
    public void start() {
        if (isRunning) {
            System.out.println("[MultiReactor] 已经在运行中");
            return;
        }
        
        isRunning = true;
        
        // 先启动所有子 Reactor
        System.out.println("[MultiReactor] 正在启动 " + subReactorCount + " 个子 Reactor...");
        for (SubReactor subReactor : subReactors) {
            subReactor.start();
        }
        
        // 等待子 Reactor 启动完成
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 最后启动主 Reactor
        System.out.println("[MultiReactor] 正在启动主 Reactor...");
        mainReactor.start();
        
        System.out.println("[MultiReactor] 所有 Reactor 已启动，服务器运行在端口: " + port);
    }
    
    /**
     * 停止所有 Reactor
     * 先停止主 Reactor（不再接受新连接），再停止所有子 Reactor
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        
        System.out.println("[MultiReactor] 正在停止所有 Reactor...");
        
        // 先停止主 Reactor
        if (mainReactor != null) {
            mainReactor.stop();
        }
        
        // 等待主 Reactor 停止
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 停止所有子 Reactor
        for (SubReactor subReactor : subReactors) {
            if (subReactor != null) {
                subReactor.stop();
            }
        }
        
        System.out.println("[MultiReactor] 所有 Reactor 已停止");
    }
    
    /**
     * 获取当前总连接数
     * 遍历所有子 Reactor 统计连接数
     */
    public int getTotalConnectionCount() {
        int total = 0;
        for (SubReactor subReactor : subReactors) {
            total += subReactor.getConnectionCount();
        }
        return total;
    }
    
    /**
     * 获取所有连接（用于广播等操作）
     * 收集所有子 Reactor 的连接
     */
    public CopyOnWriteArrayList<java.nio.channels.SocketChannel> getAllConnections() {
        CopyOnWriteArrayList<java.nio.channels.SocketChannel> allConnections = 
            new CopyOnWriteArrayList<>();
        for (SubReactor subReactor : subReactors) {
            allConnections.addAll(subReactor.getConnections());
        }
        return allConnections;
    }
    
    /**
     * 检查是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * 获取端口号
     */
    public int getPort() {
        return port;
    }
    
    /**
     * 获取子 Reactor 数量
     */
    public int getSubReactorCount() {
        return subReactorCount;
    }
}
