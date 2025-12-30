package com.gameengine.net;

import com.gameengine.game.GameObjectRecord;
import com.gameengine.game.InputRecord;
import com.gameengine.game.Record;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * 网络性能测试套件
 * 测试大型网络多连接状态下的游戏性能
 * 
 * 测试范围：
 * 1. MultiReactor - 多连接连接处理性能
 * 2. NetworkBuffer - 插值缓冲区性能和内存使用
 * 3. NetState - 状态管理并发性能
 * 4. NioClient - 客户端连接和数据处理性能
 * 5. 综合性能测试 - 端到端系统性能
 */
public class NetworkPerformanceTest {
    
    private static final String HOST = "127.0.0.1";
    private static final int BASE_PORT = 9000;
    private static final int TEST_DURATION_MS = 5000; // 5秒测试时长
    private static final Random RANDOM = new Random();
    
    /**
     * 测试1: MultiReactor连接处理性能
     * 测试大量客户端连接的处理能力
     */
    @Test
    public void testMultiReactorConnectionPerformance() throws Exception {
        NetworkTestSupport.assumeLoopbackSocketsAllowed();
        System.out.println("\n========== 测试1: MultiReactor连接处理性能 ==========");
        
        int port = BASE_PORT + 1;
        int subReactorCount = 4; // 使用4个子Reactor
        int clientCount = 100; // 100个并发客户端
        
        MultiReactor server = new MultiReactor(port, subReactorCount);
        server.start();
        
        // 等待服务器启动
        Thread.sleep(500);
        
        CountDownLatch connectLatch = new CountDownLatch(clientCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        // 并发连接测试
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try {
                    SocketChannel channel = SocketChannel.open();
                    channel.configureBlocking(true);
                    channel.connect(new InetSocketAddress(HOST, port));
                    
                    // 发送JOIN请求
                    ByteBuffer out = ByteBuffer.wrap(("JOIN:Client-" + clientId + "\n").getBytes());
                    while (out.hasRemaining()) channel.write(out);
                    
                    // 等待JOIN-ACK
                    ByteBuffer in = ByteBuffer.allocate(256);
                    StringBuilder sb = new StringBuilder();
                    long deadline = System.currentTimeMillis() + 2000;
                    boolean joined = false;
                    
                    while (System.currentTimeMillis() < deadline && !joined) {
                        in.clear();
                        int n = channel.read(in);
                        if (n > 0) {
                            sb.append(new String(in.array(), 0, n));
                            if (sb.indexOf("JOIN-ACK") >= 0) {
                                joined = true;
                                successCount.incrementAndGet();
                                break;
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    }
                    
                    if (!joined) {
                        failCount.incrementAndGet();
                    }
                    
                    channel.close();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    connectLatch.countDown();
                }
            });
        }
        
        // 等待所有连接完成
        connectLatch.await(10, TimeUnit.SECONDS);
        long endTime = System.currentTimeMillis();
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);
        
        server.stop();
        Thread.sleep(500);
        
        // 输出结果
        long duration = endTime - startTime;
        System.out.println("客户端数量: " + clientCount);
        System.out.println("子Reactor数量: " + subReactorCount);
        System.out.println("成功连接: " + successCount.get());
        System.out.println("失败连接: " + failCount.get());
        System.out.println("总耗时: " + duration + "ms");
        System.out.println("平均连接时间: " + (duration / (double) clientCount) + "ms/客户端");
        System.out.println("连接速率: " + (successCount.get() * 1000.0 / duration) + " 连接/秒");
        
        assertTrue("连接成功率应该 > 80%", successCount.get() > clientCount * 0.8);
        assertTrue("应该有连接成功", successCount.get() > 0);
    }
    
    /**
     * 测试2: NetworkBuffer插值缓冲区性能
     * 测试高频率数据推送和采样性能
     */
    @Test
    public void testNetworkBufferPerformance() {
        System.out.println("\n========== 测试2: NetworkBuffer插值缓冲区性能 ==========");
        
        int entityCount = 50; // 50个实体
        int pushCount = 1000; // 推送1000帧
        int sampleCount = 2000; // 采样2000次
        
        // 创建测试数据
        Record testRecord = createTestRecord(entityCount);
        
        // 测试推送性能（不包含sleep，只测试实际操作性能）
        long pushStart = System.nanoTime();
        for (int i = 0; i < pushCount; i++) {
            NetworkBuffer.pushRecord(testRecord);
        }
        long pushEnd = System.nanoTime();
        double pushTime = (pushEnd - pushStart) / 1_000_000.0;
        
        // 测试采样性能
        long sampleStart = System.nanoTime();
        Map<String, float[]> results = null;
        for (int i = 0; i < sampleCount; i++) {
            results = NetworkBuffer.sample();
        }
        long sampleEnd = System.nanoTime();
        double sampleTime = (sampleEnd - sampleStart) / 1_000_000.0;
        
        // 输出结果
        System.out.println("实体数量: " + entityCount);
        System.out.println("推送帧数: " + pushCount);
        System.out.println("采样次数: " + sampleCount);
        System.out.println("推送总耗时: " + String.format("%.2f", pushTime) + "ms");
        System.out.println("采样总耗时: " + String.format("%.2f", sampleTime) + "ms");
        System.out.println("平均推送时间: " + String.format("%.4f", pushTime / pushCount) + "ms/帧");
        System.out.println("平均采样时间: " + String.format("%.4f", sampleTime / sampleCount) + "ms/次");
        System.out.println("推送吞吐量: " + String.format("%.0f", pushCount * 1000.0 / pushTime) + " 帧/秒");
        System.out.println("采样吞吐量: " + String.format("%.0f", sampleCount * 1000.0 / sampleTime) + " 次/秒");
        
        assertNotNull("采样结果不应为空", results);
        // 性能断言：1000次推送应该 < 500ms（平均每次 < 0.5ms，考虑到同步操作和GC）
        assertTrue("推送性能应该 < 500ms (1000次操作，实际耗时: " + String.format("%.2f", pushTime) + "ms)", pushTime < 500);
        // 性能断言：2000次采样应该 < 200ms（平均每次 < 0.1ms，考虑到同步操作）
        assertTrue("采样性能应该 < 200ms (2000次操作，实际耗时: " + String.format("%.2f", sampleTime) + "ms)", sampleTime < 200);
    }
    
    /**
     * 测试3: NetState并发状态管理性能
     * 测试多线程并发更新和读取状态的性能
     */
    @Test
    public void testNetStateConcurrencyPerformance() throws Exception {
        System.out.println("\n========== 测试3: NetState并发状态管理性能 ==========");
        
        // 重置状态
        NetState.reset();
        
        int threadCount = 50; // 50个并发线程
        int operationsPerThread = 1000; // 每个线程1000次操作
        
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicLong totalOps = new AtomicLong(0);
        AtomicLong errorCount = new AtomicLong(0);
        
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        // 随机操作：更新输入或读取状态
                        if (RANDOM.nextBoolean()) {
                            String addressId = "/127.0.0.1:" + (50000 + threadId);
                            InputRecord input = new InputRecord(
                                addressId,
                                RANDOM.nextFloat() * 200f - 100f,
                                RANDOM.nextFloat() * 200f - 100f
                            );
                            NetState.updateClientInput(input);
                            totalOps.incrementAndGet();
                        } else {
                            String addressId = "/127.0.0.1:" + (50000 + RANDOM.nextInt(threadCount));
                            NetState.getClientInput(addressId);
                            totalOps.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        long endTime = System.nanoTime();
        executor.shutdown();
        
        double duration = (endTime - startTime) / 1_000_000.0;
        long totalOperations = totalOps.get();
        
        // 输出结果
        System.out.println("并发线程数: " + threadCount);
        System.out.println("每线程操作数: " + operationsPerThread);
        System.out.println("总操作数: " + totalOperations);
        System.out.println("错误次数: " + errorCount.get());
        System.out.println("总耗时: " + String.format("%.2f", duration) + "ms");
        System.out.println("操作吞吐量: " + String.format("%.0f", totalOperations * 1000.0 / duration) + " 操作/秒");
        System.out.println("平均操作时间: " + String.format("%.4f", duration / totalOperations) + "ms/操作");
        
        assertTrue("错误率应该 < 1%", errorCount.get() < totalOperations * 0.01);
        assertTrue("吞吐量应该 > 10000 操作/秒", totalOperations * 1000.0 / duration > 10000);
    }
    
    /**
     * 测试4: 客户端连接和数据收发性能
     * 测试客户端连接、发送输入和接收状态广播的性能
     */
    @Test
    public void testClientDataTransferPerformance() throws Exception {
        NetworkTestSupport.assumeLoopbackSocketsAllowed();
        System.out.println("\n========== 测试4: 客户端连接和数据收发性能 ==========");
        
        int port = BASE_PORT + 2;
        int clientCount = 20; // 20个客户端
        
        // 启动服务器
        MultiReactor server = new MultiReactor(port, 2);
        server.start();
        Thread.sleep(500);
        
        // 设置测试用的Record
        Record testRecord = createTestRecord(30);
        NetState.currentRecords = testRecord;
        
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch latch = new CountDownLatch(clientCount);
        AtomicLong totalSent = new AtomicLong(0);
        AtomicLong totalReceived = new AtomicLong(0);
        AtomicInteger successCount = new AtomicInteger(0);
        
        long testDuration = TEST_DURATION_MS;
        long startTime = System.currentTimeMillis();
        com.google.gson.Gson gson = new com.google.gson.Gson();
        
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try (SocketChannel channel = SocketChannel.open()) {
                    channel.configureBlocking(true);
                    channel.connect(new InetSocketAddress(HOST, port));
                    
                    String addrId = channel.getLocalAddress().toString();
                    
                    // JOIN
                    ByteBuffer out = ByteBuffer.wrap(("JOIN:Client-" + clientId + "\n").getBytes());
                    while (out.hasRemaining()) channel.write(out);
                    
                    // 等待JOIN-ACK
                    ByteBuffer in = ByteBuffer.allocate(256);
                    StringBuilder sb = new StringBuilder();
                    long deadline = System.currentTimeMillis() + 2000;
                    boolean joined = false;
                    
                    while (System.currentTimeMillis() < deadline && !joined) {
                        in.clear();
                        int n = channel.read(in);
                        if (n > 0) {
                            sb.append(new String(in.array(), 0, n));
                            if (sb.indexOf("JOIN-ACK") >= 0) {
                                joined = true;
                                successCount.incrementAndGet();
                                break;
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    }
                    
                    if (!joined) {
                        return;
                    }
                    
                    // 持续发送输入和接收数据
                    long clientEndTime = System.currentTimeMillis() + testDuration;
                    ByteBuffer inputBuffer = ByteBuffer.allocate(256);
                    ByteBuffer stateBuffer = ByteBuffer.allocate(4096);
                    
                    while (System.currentTimeMillis() < clientEndTime) {
                        // 发送输入
                        InputRecord input = new InputRecord(
                            addrId,
                            RANDOM.nextFloat() * 200f - 100f,
                            RANDOM.nextFloat() * 200f - 100f
                        );
                        String inputJson = gson.toJson(input) + "\n";
                        inputBuffer.clear();
                        inputBuffer.put(inputJson.getBytes());
                        inputBuffer.flip();
                        while (inputBuffer.hasRemaining()) {
                            channel.write(inputBuffer);
                        }
                        totalSent.incrementAndGet();
                        
                        // 尝试接收数据（非阻塞）
                        stateBuffer.clear();
                        int n = channel.read(stateBuffer);
                        if (n > 0) {
                            totalReceived.incrementAndGet();
                        }
                        
                        Thread.sleep(16); // ~60fps
                    }
                    
                } catch (Exception e) {
                    // 忽略错误
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(testDuration + 2000, TimeUnit.MILLISECONDS);
        executor.shutdown();
        
        long actualDuration = System.currentTimeMillis() - startTime;
        server.stop();
        Thread.sleep(500);
        
        // 输出结果
        System.out.println("客户端数量: " + clientCount);
        System.out.println("成功连接: " + successCount.get());
        System.out.println("测试时长: " + actualDuration + "ms");
        System.out.println("总发送包数: " + totalSent.get());
        System.out.println("总接收包数: " + totalReceived.get());
        System.out.println("发送速率: " + String.format("%.1f", totalSent.get() * 1000.0 / actualDuration) + " 包/秒");
        System.out.println("接收速率: " + String.format("%.1f", totalReceived.get() * 1000.0 / actualDuration) + " 包/秒");
        
        assertTrue("应该有客户端成功连接", successCount.get() > 0);
    }
    
    /**
     * 测试5: 综合性能测试 - 端到端系统性能
     * 测试完整的多客户端场景下的系统性能
     */
    @Test
    public void testEndToEndPerformance() throws Exception {
        NetworkTestSupport.assumeLoopbackSocketsAllowed();
        System.out.println("\n========== 测试5: 综合性能测试 ==========");
        
        int port = BASE_PORT + 3;
        int subReactorCount = 4;
        int clientCount = 50; // 50个客户端
        int testDurationSeconds = 3;
        
        // 启动服务器
        MultiReactor server = new MultiReactor(port, subReactorCount);
        server.start();
        Thread.sleep(500);
        
        // 设置游戏状态
        Record gameRecord = createTestRecord(40);
        NetState.currentRecords = gameRecord;
        
        ExecutorService executor = Executors.newFixedThreadPool(clientCount);
        CountDownLatch latch = new CountDownLatch(clientCount);
        AtomicLong totalInputSent = new AtomicLong(0);
        AtomicLong totalStateReceived = new AtomicLong(0);
        AtomicInteger connectedCount = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        long endTime = startTime + testDurationSeconds * 1000L;
        
        for (int i = 0; i < clientCount; i++) {
            final int clientId = i;
            executor.submit(() -> {
                try (SocketChannel channel = SocketChannel.open()) {
                    channel.configureBlocking(true);
                    channel.connect(new InetSocketAddress(HOST, port));
                    
                    String addrId = channel.getLocalAddress().toString();
                    
                    // JOIN
                    ByteBuffer out = ByteBuffer.wrap(("JOIN:Client-" + clientId + "\n").getBytes());
                    while (out.hasRemaining()) channel.write(out);
                    
                    // 等待JOIN-ACK
                    ByteBuffer in = ByteBuffer.allocate(256);
                    StringBuilder sb = new StringBuilder();
                    long deadline = System.currentTimeMillis() + 2000;
                    boolean joined = false;
                    
                    while (System.currentTimeMillis() < deadline && !joined) {
                        in.clear();
                        int n = channel.read(in);
                        if (n > 0) {
                            sb.append(new String(in.array(), 0, n));
                            if (sb.indexOf("JOIN-ACK") >= 0) {
                                joined = true;
                                connectedCount.incrementAndGet();
                                break;
                            }
                        } else {
                            Thread.sleep(10);
                        }
                    }
                    
                    if (!joined) {
                        return;
                    }
                    
                    // 持续发送输入和接收状态
                    com.google.gson.Gson gson = new com.google.gson.Gson();
                    ByteBuffer inputBuffer = ByteBuffer.allocate(256);
                    ByteBuffer stateBuffer = ByteBuffer.allocate(4096);
                    StringBuilder stateSb = new StringBuilder();
                    
                    while (System.currentTimeMillis() < endTime) {
                        // 发送输入
                        InputRecord input = new InputRecord(
                            addrId,
                            RANDOM.nextFloat() * 200f - 100f,
                            RANDOM.nextFloat() * 200f - 100f
                        );
                        String inputJson = gson.toJson(input) + "\n";
                        inputBuffer.clear();
                        inputBuffer.put(inputJson.getBytes());
                        inputBuffer.flip();
                        while (inputBuffer.hasRemaining()) {
                            channel.write(inputBuffer);
                        }
                        totalInputSent.incrementAndGet();
                        
                        // 尝试接收状态（非阻塞读取）
                        stateBuffer.clear();
                        int n = channel.read(stateBuffer);
                        if (n > 0) {
                            stateSb.append(new String(stateBuffer.array(), 0, n));
                            int newlineIdx;
                            while ((newlineIdx = stateSb.indexOf("\n")) >= 0) {
                                stateSb.delete(0, newlineIdx + 1);
                                totalStateReceived.incrementAndGet();
                            }
                        }
                        
                        Thread.sleep(16); // ~60fps
                    }
                    
                } catch (Exception e) {
                    // 忽略错误
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(testDurationSeconds + 5, TimeUnit.SECONDS);
        executor.shutdown();
        
        long actualDuration = System.currentTimeMillis() - startTime;
        server.stop();
        Thread.sleep(500);
        
        // 输出结果
        System.out.println("子Reactor数量: " + subReactorCount);
        System.out.println("客户端数量: " + clientCount);
        System.out.println("成功连接: " + connectedCount.get());
        System.out.println("测试时长: " + actualDuration + "ms");
        System.out.println("总发送输入包: " + totalInputSent.get());
        System.out.println("总接收状态包: " + totalStateReceived.get());
        System.out.println("输入发送速率: " + String.format("%.1f", totalInputSent.get() * 1000.0 / actualDuration) + " 包/秒");
        System.out.println("状态接收速率: " + String.format("%.1f", totalStateReceived.get() * 1000.0 / actualDuration) + " 包/秒");
        System.out.println("平均延迟: " + String.format("%.2f", actualDuration / (double) connectedCount.get()) + "ms/客户端");
        
        assertTrue("连接成功率应该 > 70%", connectedCount.get() > clientCount * 0.7);
        assertTrue("应该有数据发送", totalInputSent.get() > 0);
    }
    
    /**
     * 测试6: NetworkBuffer内存使用测试
     * 测试缓冲区在长时间运行下的内存使用情况
     */
    @Test
    public void testNetworkBufferMemoryUsage() {
        System.out.println("\n========== 测试6: NetworkBuffer内存使用测试 ==========");
        
        int entityCount = 100;
        int pushCount = 5000;
        
        Record testRecord = createTestRecord(entityCount);
        
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // 推送大量数据
        for (int i = 0; i < pushCount; i++) {
            NetworkBuffer.pushRecord(testRecord);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // 采样多次
        for (int i = 0; i < 1000; i++) {
            NetworkBuffer.sample();
        }
        
        runtime.gc();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("实体数量: " + entityCount);
        System.out.println("推送帧数: " + pushCount);
        System.out.println("内存使用: " + String.format("%.2f", memoryUsed / 1024.0 / 1024.0) + "MB");
        System.out.println("平均每帧内存: " + String.format("%.2f", memoryUsed / (double) pushCount) + " bytes");
        
        assertTrue("内存使用应该 < 50MB", memoryUsed < 50 * 1024 * 1024);
    }
    
    // ========== 辅助方法 ==========
    
    /**
     * 创建测试用的Record对象
     */
    private Record createTestRecord(int entityCount) {
        Record record = new Record();
        record.setRecordType("object_move");
        record.setKey(System.currentTimeMillis() / 1000.0f);
        
        List<GameObjectRecord> entities = new ArrayList<>();
        for (int i = 0; i < entityCount; i++) {
            GameObjectRecord entity = new GameObjectRecord();
            entity.id = "Entity-" + i;
            entity.x = RANDOM.nextFloat() * 1000f;
            entity.y = RANDOM.nextFloat() * 1000f;
            entities.add(entity);
        }
        record.getGameObjectsMove().addAll(entities);
        
        return record;
    }
    
}
