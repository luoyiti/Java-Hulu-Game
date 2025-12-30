package com.gameengine.game;

/**
 * 玩家的输入集合
 * 用作网络通信中，对客户端玩家操作行为的记录
 */
public class InputRecord {
    public String type = "INPUT";
    public String addressId;  // 使用字符串存储地址，便于 JSON 序列化
    public float vx;
    public float vy;
    
    public InputRecord() {}
    
    public InputRecord(String addressId, float vx, float vy) {
        this.addressId = addressId;
        this.vx = vx;
        this.vy = vy;
    }
}
