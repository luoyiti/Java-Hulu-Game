package com.gameengine.game;

import com.gameengine.components.RenderComponent.RenderType;

public class GameObjectRecord {
    public String id;
    // 坐标位置
    public float x;
    public float y;
    // 图形信息
    // 所有值都默认为null，在这种情况下 gson 不会都记录下来
    public RenderType rt;
    public float height;
    public float width;
    public float alpha;
    public String imagePath;
    public float r;
    public float g;
    public float b;
    public float a;
    public int segments;
    public float rotation;
    
    // 生命值信息（用于渲染血条）
    public int currentHealth;
    public int maxHealth;
    
    // 对象身份标识（Player, Enemy, Player Skill, Enemy Skill 等）
    public String identity;
}
