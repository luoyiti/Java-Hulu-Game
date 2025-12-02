package com.gameengine.game;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

import java.util.Random;

/**
 * 敌人士兵类
 * 普通敌人单位
 */
public class EnemySoldier extends GameObject {
    
    private IRenderer renderer;
    private String imagePath;
    private Vector2 imageSize;
    
    public EnemySoldier(IRenderer renderer, Vector2 position, String imagePath, Vector2 imageSize, Random random) {
        super("EnemySoldier");
        this.renderer = renderer;
        this.imagePath = imagePath;
        this.imageSize = imageSize;
        this.setEnemy();
        
        // 添加变换组件
        this.addComponent(new TransformComponent(position));
        
        // 添加图片渲染组件
        RenderComponent render = this.addComponent(new RenderComponent(imagePath, imageSize));
        render.setRenderer(renderer);
        
        // 添加物理组件
        PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.5f));
        physics.setVelocity(new Vector2(
            (random.nextFloat() - 0.5f) * 100,
            (random.nextFloat() - 0.5f) * 100
        ));
        physics.setFriction(0.98f);
        
        // 添加生命特征组件
        this.addComponent(new LifeFeatureComponent(100));
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateComponents(deltaTime);
    }
    
    @Override
    public void render() {
        // 使用 RenderComponent 进行图片渲染
        RenderComponent render = getComponent(RenderComponent.class);
        if (render != null) {
            render.render();
        }
        
        // 渲染血条
        LifeFeatureComponent lifeFeature = getComponent(LifeFeatureComponent.class);
        if (lifeFeature != null) {
            lifeFeature.render();
        }
    }

    /**
     * 原始手绘渲染方法（保留作为备用）
     */
//     private void renderBodyParts() {
//         TransformComponent transform = getComponent(TransformComponent.class);
//         if (transform == null) return;
//         Vector2 p = transform.getPosition();
        
//                 // 躯干（制服）
//         renderer.drawRect(p.x - 8f, p.y - 2f, 16f, 20f, 0.12f, 0.40f, 0.18f, 1f);
        
//                 // 头部
//         renderer.drawCircle(p.x, p.y - 14f, 6f, 24, 1.0f, 0.86f, 0.72f, 1.0f);
        
//                 // 头盔
//         renderer.drawRect(p.x - 7f, p.y - 19f, 14f, 6f, 0.10f, 0.30f, 0.12f, 1.0f);
//         renderer.drawRect(p.x - 7f, p.y - 14f, 14f, 2f, 0.08f, 0.25f, 0.10f, 1.0f);
        
//                 // 眼睛
//         renderer.drawCircle(p.x - 2.0f, p.y - 14.0f, 0.8f, 12, 0f, 0f, 0f, 1f);
//         renderer.drawCircle(p.x + 2.0f, p.y - 14.0f, 0.8f, 12, 0f, 0f, 0f, 1f);
        
//                 // 手臂（制服）
//         renderer.drawRect(p.x - 14f, p.y - 2f, 6f, 14f, 0.12f, 0.40f, 0.18f, 1f);
//         renderer.drawRect(p.x + 8f, p.y - 2f, 6f, 14f, 0.12f, 0.40f, 0.18f, 1f);
        
//                 // 腰带
//         renderer.drawRect(p.x - 8f, p.y + 6f, 16f, 2f, 0.05f, 0.05f, 0.05f, 1f);
        
//                 // 腿（裤子）
//         renderer.drawRect(p.x - 6f, p.y + 12f, 6f, 12f, 0.10f, 0.35f, 0.15f, 1f);
//         renderer.drawRect(p.x + 0f, p.y + 12f, 6f, 12f, 0.10f, 0.35f, 0.15f, 1f);
        
//                 // 靴子
//         renderer.drawRect(p.x - 6f, p.y + 22f, 6f, 3f, 0f, 0f, 0f, 1f);
//         renderer.drawRect(p.x + 0f, p.y + 22f, 6f, 3f, 0f, 0f, 0f, 1f);
        
//                 // 步枪
//         renderer.drawRect(p.x + 12f, p.y - 2f, 14f, 2f, 0.1f, 0.1f, 0.1f, 1f);
//         renderer.drawRect(p.x + 12f, p.y + 0f, 3f, 6f, 0.1f, 0.1f, 0.1f, 1f);
        
//         // 渲染血条在头顶上方
//         LifeFeatureComponent lifeFeature = getComponent(LifeFeatureComponent.class);
//         if (lifeFeature != null) {
//             int currentHealth = lifeFeature.getBlood();
//             int maxHealth = 100; // 最大血量为100
//             // 血条位于敌人头顶上方
//             renderer.drawHealthBar(p.x - 15f, p.y - 30f, 30f, 4f, currentHealth, maxHealth);
//         }
//     }
}
