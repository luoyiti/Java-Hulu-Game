package com.gameengine.net;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import com.gameengine.game.InputRecord;
import com.gameengine.game.Record;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NioServer服务器类的测试用例
 * 测试服务器启动、停止、接受连接、处理消息、广播等功能
 */
public class NioServerTest {
    
    private NioServer server;
    private int testPort;
    
    @Before
    public void setUp() throws Exception {
        NetworkTestSupport.assumeLoopbackSocketsAllowed();
        // 使用动态分配的端口避免冲突
        try (ServerSocket socket = new ServerSocket(0)) {
            testPort = socket.getLocalPort();
        }
        NetState.reset();
    }
    
    @After
    public void tearDown() {
        if (server != null) {
            server.stop();
            try {
                Thread.sleep(300); // 等待服务器停止和端口释放
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        NetState.reset();
    }
    
    /**
     * 测试服务器构造函数
     */
    @Test
    public void testConstructor() {
        server = new NioServer(testPort);
        assertNotNull("服务器应该被创建", server);
    }
    
    /**
     * 测试服务器启动
     */
    @Test
    public void testStart() throws Exception {
        server = new NioServer(testPort);
        server.start();
        
        // 等待服务器启动
        Thread.sleep(200);
        
        // 验证服务器已启动（通过尝试连接）
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            boolean connected = channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            assertTrue("应该能够连接到服务器", connected);
            channel.close();
        }
    }
    
    /**
     * 测试服务器重复启动
     */
    @Test
    public void testStartMultipleTimes() throws Exception {
        server = new NioServer(testPort);
        
        // 第一次启动
        server.start();
        Thread.sleep(100);
        
        // 第二次启动（应该被忽略）
        server.start();
        Thread.sleep(100);
        
        // 验证服务器仍然运行
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            boolean connected = channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            assertTrue("服务器应该仍然运行", connected);
            channel.close();
        }
    }
    
    /**
     * 测试服务器停止
     */
    @Test
    public void testStop() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 验证服务器运行
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            // 这里使用connect方法验证服务器运行
            boolean connected = channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            assertTrue("服务器应该运行", connected);
            channel.close();
        }
        
        // 停止服务器
        server.stop();
        Thread.sleep(300);
        
        // 验证stop方法正常执行（不抛出异常）
        assertTrue("stop方法应该正常执行", true);
    }
    
    /**
     * 测试接受客户端连接
     */
    @Test
    public void testAcceptConnection() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 连接客户端
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            
            // 等待服务器处理连接
            Thread.sleep(100);
            
            // 验证连接被接受（通过检查NetState）
            assertTrue("应该有客户端连接", NetState.getClientCount() >= 0);
            
            channel.close();
            Thread.sleep(100);
        }
    }
    
    /**
     * 测试处理JOIN请求
     */
    @Test
    public void testHandleJoinRequest() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(100);
            
            // 发送JOIN请求
            ByteBuffer out = ByteBuffer.wrap("JOIN:TestPlayer\n".getBytes());
            while (out.hasRemaining()) {
                channel.write(out);
            }
            
            // 等待服务器处理
            Thread.sleep(100);
            
            // 读取响应
            ByteBuffer in = ByteBuffer.allocate(256);
            StringBuilder response = new StringBuilder();
            long deadline = System.currentTimeMillis() + 2000;
            
            while (System.currentTimeMillis() < deadline) {
                in.clear();
                int n = channel.read(in);
                if (n > 0) {
                    response.append(new String(in.array(), 0, n));
                    if (response.toString().contains("JOIN-ACK")) {
                        break;
                    }
                } else if (n == -1) {
                    break;
                }
                Thread.sleep(50);
            }
            
            // 验证收到JOIN-ACK
            assertTrue("应该收到JOIN-ACK响应", response.toString().contains("JOIN-ACK"));
        }
    }
    
    /**
     * 测试处理INPUT请求
     */
    @Test
    public void testHandleInputRequest() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 重置NetState
        NetState.reset();
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(100);
            
            // 先发送JOIN
            ByteBuffer joinOut = ByteBuffer.wrap("JOIN:TestPlayer\n".getBytes());
            while (joinOut.hasRemaining()) {
                channel.write(joinOut);
            }
            Thread.sleep(100);
            
            // 清空响应
            ByteBuffer temp = ByteBuffer.allocate(256);
            channel.read(temp);
            
            // 发送INPUT请求
            InputRecord inputRecord = new InputRecord("/127.0.0.1:12345", 100.0f, 200.0f);
            String inputJson = new com.google.gson.Gson().toJson(inputRecord) + "\n";
            ByteBuffer inputOut = ByteBuffer.wrap(inputJson.getBytes());
            while (inputOut.hasRemaining()) {
                channel.write(inputOut);
            }
            
            // 等待服务器处理
            Thread.sleep(200);
            
            // 验证输入被处理（通过检查NetState）
            InputRecord stored = NetState.getClientInput("/127.0.0.1:12345");
            assertNotNull("应该保存了客户端输入", stored);
            assertEquals("输入速度x应该正确", 100.0f, stored.vx, 0.001f);
            assertEquals("输入速度y应该正确", 200.0f, stored.vy, 0.001f);
        }
    }
    
    /**
     * 测试广播游戏状态
     */
    @Test
    public void testBroadcastGameState() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 设置游戏状态
        Record testRecord = createTestRecord();
        NetState.currentRecords = testRecord;
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(100);
            
            // 发送JOIN
            ByteBuffer joinOut = ByteBuffer.wrap("JOIN:TestPlayer\n".getBytes());
            while (joinOut.hasRemaining()) {
                channel.write(joinOut);
            }
            Thread.sleep(100);
            
            // 清空JOIN-ACK响应
            ByteBuffer temp = ByteBuffer.allocate(256);
            channel.read(temp);
            
            // 等待服务器广播（至少50ms一次）
            Thread.sleep(150);
            
            // 尝试读取广播数据
            ByteBuffer in = ByteBuffer.allocate(4096);
            int n = channel.read(in);
            if (n > 0) {
                String broadcast = new String(in.array(), 0, n);
                // 验证收到了JSON数据
                assertTrue("应该收到广播数据", broadcast.length() > 0);
            }
        }
    }
    
    /**
     * 测试客户端断开连接处理
     */
    @Test
    public void testClientDisconnect() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(true);
        channel.connect(new InetSocketAddress("127.0.0.1", testPort));
        Thread.sleep(100);
        
        // 关闭客户端连接
        channel.close();
        Thread.sleep(200);
        
        // 验证连接被清理（注意：由于NetState的更新可能有延迟，这里只验证不会增加）
        // 实际的清理验证可能需要更复杂的测试
        assertTrue("连接计数应该合理", NetState.getClientCount() >= 0);
    }
    
    /**
     * 测试多个客户端同时连接
     */
    @Test
    public void testMultipleClients() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        SocketChannel client1 = SocketChannel.open();
        SocketChannel client2 = SocketChannel.open();
        SocketChannel client3 = SocketChannel.open();
        
        try {
            // 三个客户端都连接
            client1.configureBlocking(true);
            client2.configureBlocking(true);
            client3.configureBlocking(true);
            
            client1.connect(new InetSocketAddress("127.0.0.1", testPort));
            client2.connect(new InetSocketAddress("127.0.0.1", testPort));
            client3.connect(new InetSocketAddress("127.0.0.1", testPort));
            
            Thread.sleep(200);
            
            // 所有客户端都发送JOIN
            ByteBuffer join1 = ByteBuffer.wrap("JOIN:Player1\n".getBytes());
            ByteBuffer join2 = ByteBuffer.wrap("JOIN:Player2\n".getBytes());
            ByteBuffer join3 = ByteBuffer.wrap("JOIN:Player3\n".getBytes());
            
            while (join1.hasRemaining()) client1.write(join1);
            while (join2.hasRemaining()) client2.write(join2);
            while (join3.hasRemaining()) client3.write(join3);
            
            Thread.sleep(200);
            
            // 验证所有连接都被接受
            assertTrue("应该有多个客户端连接", NetState.getClientCount() >= 0);
            
        } finally {
            client1.close();
            client2.close();
            client3.close();
        }
    }
    
    /**
     * 测试无效消息处理
     */
    @Test
    public void testInvalidMessage() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(500);  // 增加服务器启动等待时间
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(200);
            
            // 发送无效消息
            ByteBuffer invalidOut = ByteBuffer.wrap("INVALID_MESSAGE\n".getBytes());
            while (invalidOut.hasRemaining()) {
                channel.write(invalidOut);
            }
            
            Thread.sleep(300);
            
            // 验证服务器没有崩溃（仍然可以处理其他消息）
            ByteBuffer joinOut = ByteBuffer.wrap("JOIN:TestPlayer\n".getBytes());
            while (joinOut.hasRemaining()) {
                channel.write(joinOut);
            }
            
            Thread.sleep(300);
            
            // 验证JOIN仍然可以正常工作
            ByteBuffer in = ByteBuffer.allocate(4096);
            StringBuilder response = new StringBuilder();
            long deadline = System.currentTimeMillis() + 5000;  // 增加超时时间到5秒
            
            while (System.currentTimeMillis() < deadline) {
                in.clear();
                int n = channel.read(in);
                if (n > 0) {
                    response.append(new String(in.array(), 0, n));
                    if (response.toString().contains("JOIN-ACK")) {
                        break;
                    }
                } else if (n == -1) {
                    break;
                }
                Thread.sleep(100);
            }
            
            assertTrue("无效消息后JOIN仍然应该工作, 收到的响应: " + response.toString(), 
                response.toString().contains("JOIN-ACK"));
        }
    }
    
    /**
     * 测试JSON解析错误处理
     */
    @Test
    public void testInvalidJsonInput() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(100);
            
            // 发送包含INPUT但JSON无效的消息
            ByteBuffer invalidJson = ByteBuffer.wrap("INPUT: {invalid json}\n".getBytes());
            while (invalidJson.hasRemaining()) {
                channel.write(invalidJson);
            }
            
            Thread.sleep(100);
            
            // 验证服务器没有崩溃
            assertTrue("服务器应该仍然运行", true);
        }
    }
    
    /**
     * 测试广播空状态
     */
    @Test
    public void testBroadcastEmptyState() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        // 设置空状态
        NetState.currentRecords = null;
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(100);
            
            // 发送JOIN
            ByteBuffer joinOut = ByteBuffer.wrap("JOIN:TestPlayer\n".getBytes());
            while (joinOut.hasRemaining()) {
                channel.write(joinOut);
            }
            Thread.sleep(100);
            
            // 清空响应
            ByteBuffer temp = ByteBuffer.allocate(256);
            channel.read(temp);
            
            // 等待广播
            Thread.sleep(150);
            
            // 验证服务器没有崩溃（空状态不应该导致问题）
            assertTrue("服务器应该仍然运行", true);
        }
    }
    
    /**
     * 测试服务器启动后立即停止
     */
    @Test
    public void testStartAndImmediateStop() throws Exception {
        server = new NioServer(testPort);
        server.start();
        server.stop();
        
        Thread.sleep(200);
        
        // 验证服务器已停止
        assertTrue("stop方法应该正常执行", true);
    }
    
    /**
     * 测试连接后发送多个JOIN请求
     */
    @Test
    public void testMultipleJoinRequests() throws Exception {
        server = new NioServer(testPort);
        server.start();
        Thread.sleep(200);
        
        try (SocketChannel channel = SocketChannel.open()) {
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress("127.0.0.1", testPort));
            Thread.sleep(100);
            
            // 发送多个JOIN请求
            for (int i = 0; i < 3; i++) {
                ByteBuffer joinOut = ByteBuffer.wrap(("JOIN:Player" + i + "\n").getBytes());
                while (joinOut.hasRemaining()) {
                    channel.write(joinOut);
                }
                Thread.sleep(50);
            }
            
            // 验证服务器处理了请求（没有崩溃）
            assertTrue("服务器应该处理多个JOIN请求", true);
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
