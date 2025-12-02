package com.gameengine.game;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

import java.util.Random;

/**
 * 玩家攻击技能J
 * 葫芦娃的技能1，按J触发
 * 可以向左右方向发射两道光束，造成10伤害
 */
public class AttackSkillJ extends GameObject {
    
    private IRenderer renderer;
    
    public AttackSkillJ(int index, IRenderer renderer, Random random) {
        super("Attacking SkillJ " + index);
        this.renderer = renderer;
        this.setPlayerSkill();
    }
    
    /**
     * 初始化技能位置和物理属性
     */
    public void initializePosition(Vector2 position, Random random) {
        this.addComponent(new TransformComponent(position));
        
        RenderComponent render = this.addComponent(new RenderComponent(
            RenderComponent.RenderType.RECTANGLE,
            new Vector2(20, 20),
            new RenderComponent.Color(1.0f, 0.5f, 0.0f, 1.0f)
        ));
        render.setRenderer(renderer);
        
        PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.5f));
        physics.setVelocity(new Vector2(
            (random.nextFloat() - 0.5f) * 100,
            (random.nextFloat() - 0.5f) * 100
        ));
        physics.setFriction(0.98f);
        
        // 添加生命周期组件，设置1秒生命周期
        LifeFeatureComponent lifeFeature = this.addComponent(new LifeFeatureComponent(1));
        lifeFeature.setLifetime(1.0f); // 技能存活1秒
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateComponents(deltaTime);
    }
    
    @Override
    public void render() {
        renderBodyParts();
    }
    
    private void renderBodyParts() {
        TransformComponent transform = getComponent(TransformComponent.class);
        if (transform == null) return;
        Vector2 p = transform.getPosition();
        
        // 渲染箭头形状的攻击技能
        renderer.drawRect(p.x + 5f, p.y - 4f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
        renderer.drawRect(p.x + 5f, p.y + 1f, 5f, 3f, 1.0f, 1.0f, 1.0f, 1.0f);
    }
}
