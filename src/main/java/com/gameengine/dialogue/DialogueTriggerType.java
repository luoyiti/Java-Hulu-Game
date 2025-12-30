package com.gameengine.dialogue;

/**
 * 对话触发类型枚举
 * 定义对话的触发方式
 */
public enum DialogueTriggerType {
    /**
     * 游戏开始时触发
     */
    GAME_START,
    
    /**
     * 游戏结束时触发（胜利或失败）
     */
    GAME_OVER,
    
    /**
     * 玩家胜利时触发
     */
    VICTORY,
    
    /**
     * 玩家失败时触发
     */
    DEFEAT,
    
    /**
     * 关卡完成时触发
     */
    LEVEL_COMPLETE,
    
    /**
     * 距离触发（玩家靠近时）
     */
    DISTANCE,
    
    /**
     * 手动触发（按键交互）
     */
    MANUAL,
    
    /**
     * Boss战开始时触发
     */
    BOSS_ENCOUNTER,
    
    /**
     * 自定义事件触发
     */
    CUSTOM
}
