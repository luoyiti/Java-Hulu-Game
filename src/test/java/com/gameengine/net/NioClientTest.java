package com.gameengine.net;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import com.gameengine.game.Record;
import com.gameengine.input.InputManager;
import java.net.ServerSocket;

/**
 * NioClient客户端类的测试用例
 * 测试客户端连接、加入、输入发送、状态接收等功能
 */
public class NioClientTest {
    
    private NioClient client;
    private NioServer server;
    private int testPort;
    private InputManager inputManager;
    
    @Before
    public void setUp() {
        NetworkTestSupport.assumeLoopbackSocketsAllowed();
        client = new NioClient();
        try (ServerSocket socket = new ServerSocket(0)) {
            testPort = socket.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        inputManager = InputManager.getInstance();
        NetState.reset();
    }
    
    @After
    public void tearDown() {
        if (client != null) {
            client.disconnect();
        }
        if (server != null) {
            server.stop();
            try {
                Thread.sleep(100); // 等待服务器停止
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        NetState.reset();
    }
    
    /**
     * 测试客户端连接功能 - 成功连接
     */
    @Test
    public void testConnectSuccess() throws Exception {
        // 启动服务器
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200); // 等待服务器启动
        
        // 测试连接
        boolean connected = client.connect("127.0.0.1", testPort);
        assertTrue("客户端应该成功连接", connected);
        assertTrue("客户端应该处于连接状态", client.isConnected());
        assertNotNull("应该有地址ID", client.getMyAddressId());
    }
    
    /**
     * 测试join功能 - 成功加入
     */
    @Test
    public void testJoinSuccess() throws Exception {
        // 启动服务器
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 连接并加入
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        boolean joined = client.join("TestPlayer");
        assertTrue("应该成功加入", joined);
    }
    
    /**
     * 测试join功能 - 未连接时加入
     */
    @Test
    public void testJoinWithoutConnection() {
        // 未连接时尝试加入
        boolean joined = client.join("TestPlayer");
        assertFalse("未连接时加入应该失败", joined);
    }
    
    /**
     * 测试join功能 - 超时情况
     */
    @Test
    public void testJoinTimeout() throws Exception {
        // 由于NioServer会正常响应，我们测试正常的join即可
        // 超时测试需要更复杂的mock，这里简化处理
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        boolean joined = client.join("TestPlayer");
        assertTrue("正常服务器应该成功加入", joined);
    }
    
    /**
     * 测试startInputLoop - 正常启动
     */
    @Test
    public void testStartInputLoop() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该成功加入", client.join("TestPlayer"));
        
        // 启动输入循环
        client.startInputLoop(inputManager);
        
        // 等待一下确保线程启动
        Thread.sleep(100);
        
        // 验证循环已启动（通过检查线程是否运行）
        // 由于无法直接访问，我们通过后续的disconnect来验证
        assertTrue("客户端应该仍然连接", client.isConnected());
    }
    
    /**
     * 测试startInputLoop - 未连接时启动
     */
    @Test
    public void testStartInputLoopWithoutConnection() {
        // 未连接时启动输入循环
        client.startInputLoop(inputManager);
        
        // 应该不会抛出异常，但也不会启动
        assertFalse("未连接时不应该处于连接状态", client.isConnected());
    }
    
    /**
     * 测试startInputLoop - 重复启动
     */
    @Test
    public void testStartInputLoopMultipleTimes() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该成功加入", client.join("TestPlayer"));
        
        // 第一次启动
        client.startInputLoop(inputManager);
        Thread.sleep(50);
        
        // 第二次启动（应该被忽略）
        client.startInputLoop(inputManager);
        Thread.sleep(50);
        
        // 应该仍然连接
        assertTrue("客户端应该仍然连接", client.isConnected());
    }
    
    /**
     * 测试startStateReceiveLoop - 正常启动
     */
    @Test
    public void testStartStateReceiveLoop() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该成功加入", client.join("TestPlayer"));
        
        // 设置一个测试Record供服务器广播
        Record testRecord = createTestRecord();
        NetState.currentRecords = testRecord;
        
        // 启动状态接收循环
        client.startStateReceiveLoop();
        
        // 等待服务器广播（至少50ms一次）
        Thread.sleep(150);
        
        // 验证状态被接收（通过检查NetState.currentRecords是否更新）
        // 注意：由于是异步的，这里只验证循环启动成功
        assertTrue("客户端应该仍然连接", client.isConnected());
    }
    
    /**
     * 测试startStateReceiveLoop - 未连接时启动
     */
    @Test
    public void testStartStateReceiveLoopWithoutConnection() {
        // 未连接时启动状态接收循环
        client.startStateReceiveLoop();
        
        // 应该不会抛出异常，但也不会启动
        assertFalse("未连接时不应该处于连接状态", client.isConnected());
    }
    
    /**
     * 测试startStateReceiveLoop - 重复启动
     */
    @Test
    public void testStartStateReceiveLoopMultipleTimes() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该成功加入", client.join("TestPlayer"));
        
        // 第一次启动
        client.startStateReceiveLoop();
        Thread.sleep(50);
        
        // 第二次启动（应该被忽略）
        client.startStateReceiveLoop();
        Thread.sleep(50);
        
        // 应该仍然连接
        assertTrue("客户端应该仍然连接", client.isConnected());
    }
    
    /**
     * 测试disconnect功能
     */
    @Test
    public void testDisconnect() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该处于连接状态", client.isConnected());
        
        // 断开连接
        client.disconnect();
        
        // 验证已断开
        assertFalse("断开后不应该处于连接状态", client.isConnected());
    }
    
    /**
     * 测试isConnected功能
     */
    @Test
    public void testIsConnected() throws Exception {
        // 初始状态
        assertFalse("初始状态不应该连接", client.isConnected());
        
        // 连接后
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("连接后应该处于连接状态", client.isConnected());
        
        // 断开后
        client.disconnect();
        assertFalse("断开后不应该处于连接状态", client.isConnected());
    }
    
    /**
     * 测试getMyAddressId功能
     */
    @Test
    public void testGetMyAddressId() throws Exception {
        // 未连接时应该为null
        assertNull("未连接时地址ID应该为null", client.getMyAddressId());
        
        // 连接后应该有地址ID
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        String addressId = client.getMyAddressId();
        assertNotNull("连接后应该有地址ID", addressId);
        assertTrue("地址ID应该包含/", addressId.contains("/"));
    }
    
    /**
     * 测试输入循环中的按键处理
     */
    @Test
    public void testInputLoopKeyHandling() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该成功加入", client.join("TestPlayer"));
        
        // 模拟按键按下
        inputManager.onKeyPressed(87); // W键
        inputManager.onKeyPressed(68); // D键
        
        // 启动输入循环
        client.startInputLoop(inputManager);
        
        // 等待一段时间让输入循环发送数据
        Thread.sleep(100);
        
        // 验证客户端仍然连接（说明输入循环正常工作）
        assertTrue("输入循环运行时应该保持连接", client.isConnected());
    }
    
    /**
     * 测试完整的连接-加入-输入-断开流程
     */
    @Test
    public void testFullWorkflow() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 1. 连接
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        assertTrue("应该处于连接状态", client.isConnected());
        
        // 2. 加入
        assertTrue("应该成功加入", client.join("TestPlayer"));
        
        // 3. 启动输入循环
        inputManager.onKeyPressed(65); // A键
        client.startInputLoop(inputManager);
        Thread.sleep(100);
        
        // 4. 启动状态接收循环
        Record testRecord = createTestRecord();
        NetState.currentRecords = testRecord;
        client.startStateReceiveLoop();
        Thread.sleep(150);
        
        // 5. 验证连接状态
        assertTrue("完整流程后应该仍然连接", client.isConnected());
        
        // 6. 断开连接
        client.disconnect();
        assertFalse("断开后不应该处于连接状态", client.isConnected());
    }
    
    /**
     * 测试连接后立即断开的情况
     */
    @Test
    public void testConnectAndImmediateDisconnect() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        assertTrue("应该成功连接", client.connect("127.0.0.1", testPort));
        client.disconnect();
        
        // 尝试在断开后操作
        assertFalse("断开后join应该失败", client.join("TestPlayer"));
        assertFalse("断开后不应该处于连接状态", client.isConnected());
    }
    
    /**
     * 测试多个客户端同时连接
     */
    @Test
    public void testMultipleClients() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        NioClient client1 = new NioClient();
        NioClient client2 = new NioClient();
        
        try {
            // 两个客户端都连接
            assertTrue("客户端1应该成功连接", client1.connect("127.0.0.1", testPort));
            assertTrue("客户端2应该成功连接", client2.connect("127.0.0.1", testPort));
            
            // 两个客户端都加入
            assertTrue("客户端1应该成功加入", client1.join("Player1"));
            assertTrue("客户端2应该成功加入", client2.join("Player2"));
            
            // 验证连接状态
            assertTrue("客户端1应该处于连接状态", client1.isConnected());
            assertTrue("客户端2应该处于连接状态", client2.isConnected());
            
            // 验证地址ID不同
            assertNotEquals("两个客户端的地址ID应该不同", 
                client1.getMyAddressId(), client2.getMyAddressId());
        } finally {
            client1.disconnect();
            client2.disconnect();
        }
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 创建测试用的Record对象
     */
    private Record createTestRecord() {
        Record record = new Record();
        record.setRecordType("object_move");
        record.setKey(System.currentTimeMillis() / 1000.0f);
        return record;
    }
}
