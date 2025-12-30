package com.gameengine.game;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.SerializedName;

/**
 * 这里用于定义记录的数据类型
 */

enum RecordType {
        @SerializedName("input")
        INPUT,
        @SerializedName("object_move")
        OBJECT_MOVE
    }

public class Record {
    float key;
    RecordType type;
    List<GameObjectRecord> gameObjectMove;
    List<InputRecord> inputRecords;
    
    // 全局游戏状态
    int playerHealth;           // 玩家当前血量
    int playerMaxHealth;        // 玩家最大血量
    float skillCooldownPercent; // 技能冷却百分比 (0.0 - 1.0)
    int currentLevel;           // 当前关卡

    public Record() {
        gameObjectMove = new ArrayList<GameObjectRecord>();
        inputRecords = new ArrayList<InputRecord>();
        playerHealth = 100;
        playerMaxHealth = 100;
        skillCooldownPercent = 1.0f;
        currentLevel = 1;
    }

    public void setRecordType(String Recordtype) {
        if (Recordtype == "input") {
            type = RecordType.INPUT;
            // 这里设置为null是为了不让gson记录他们
            gameObjectMove = null;
        } else if (Recordtype == "object_move") {
            type = RecordType.OBJECT_MOVE;
            inputRecords = null;
        }
    }

    public void setKey(float key) {
        this.key = key;
    }

    public List<GameObjectRecord> getGameObjectsMove() {
        return this.gameObjectMove;
    }

    public float getKey() {
        return this.key;
    }

    public String getType() {
        if (this.type == RecordType.INPUT) {
            return "input";
        } else if (this.type == RecordType.OBJECT_MOVE) {
            return "object_move";
        } else {
            return "null";
        }
    }
    
    // 全局状态的 getter/setter
    public int getPlayerHealth() {
        return playerHealth;
    }
    
    public void setPlayerHealth(int health) {
        this.playerHealth = health;
    }
    
    public int getPlayerMaxHealth() {
        return playerMaxHealth;
    }
    
    public void setPlayerMaxHealth(int maxHealth) {
        this.playerMaxHealth = maxHealth;
    }
    
    public float getSkillCooldownPercent() {
        return skillCooldownPercent;
    }
    
    public void setSkillCooldownPercent(float percent) {
        this.skillCooldownPercent = percent;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }
}