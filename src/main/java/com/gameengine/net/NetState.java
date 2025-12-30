package com.gameengine.net;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.gameengine.game.InputRecord;
import com.gameengine.game.Record;

/**
 * 网络状态管理类
 * 用于在服务端和客户端之间共享状态
 */
public final class NetState {
    
    // 客户端连接管理
    private static int clientCount = 0;
    
    public static void setClientCount(int count) {
        clientCount = count;
    }
    
    public static int getClientCount() {
        return clientCount;
    }

    // 客户端地址管理
    private static final List<String> clientAddresses = new ArrayList<>();
    
    public static synchronized void addClientAddress(SocketAddress address) {
        if (address != null) {
            String addrStr = address.toString();
            if (!clientAddresses.contains(addrStr)) {
                clientAddresses.add(addrStr);
            }
        }
    }
    
    public static synchronized void removeClientAddress(SocketAddress address) {
        if (address != null) {
            clientAddresses.remove(address.toString());
        }
    }
    
    public static synchronized List<String> getClientAddressesSnapshot() {
        return new ArrayList<>(clientAddresses);
    }
    
    public static synchronized int getClientAddressCount() {
        return clientAddresses.size();
    }

    // 客户端输入
    // 使用地址字符串作为 key，存储每个客户端的最新输入
    private static final Map<String, InputRecord> clientInputs = new ConcurrentHashMap<>();
    
    public static void updateClientInput(InputRecord input) {
        if (input != null && input.addressId != null) {
            clientInputs.put(input.addressId, input);
        }
    }
    
    public static InputRecord getClientInput(String addressId) {
        return clientInputs.get(addressId);
    }
    
    public static Map<String, InputRecord> getAllClientInputs() {
        return new ConcurrentHashMap<>(clientInputs);
    }
    
    public static void clearClientInput(String addressId) {
        clientInputs.remove(addressId);
    }
    
    // 重置所有状态
    public static synchronized void reset() {
        clientCount = 0;
        clientAddresses.clear();
        clientInputs.clear();
    }

    // 服务端渲染管理
    public static Record currentRecords;

}
