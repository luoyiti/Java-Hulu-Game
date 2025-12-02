package com.gameengine.game;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 敌人国王类
 * BOSS级别敌人
 */
public class EnemyKing extends GameObject {
    
    private IRenderer renderer;
    private Scene scene;
    private Random random;
    private List<GameObject> attackingSkills = new ArrayList<>();
    private String imagePath;
    private Vector2 imageSize;
    
    public EnemyKing(IRenderer renderer, Scene scene, Vector2 position, String imagePath, Vector2 imageSize, Random random) {
        super("EnemyKing");
        this.renderer = renderer;
        this.scene = scene;
        this.random = random;
        this.setEnemy();
        this.imagePath = imagePath;
        this.imageSize = imageSize;
        
        this.addComponent(new TransformComponent(position));
        
        // 添加图片渲染组件
        RenderComponent render = this.addComponent(new RenderComponent(imagePath, imageSize));
        render.setRenderer(renderer);
        
        PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.5f));
        physics.setVelocity(new Vector2(
            (random.nextFloat() - 0.5f) * 100,
            (random.nextFloat() - 0.5f) * 100
        ));
        physics.setFriction(0.98f);
        
        // 添加生命特征组件（国王有200血量）
        this.addComponent(new LifeFeatureComponent(200));
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateComponents(deltaTime);
        if (attackingSkills.isEmpty()) {
            initAttackingSkills();
        }
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

    private void renderBodyParts() {
        TransformComponent transform = getComponent(TransformComponent.class);
        if (transform == null) return;
        Vector2 p = transform.getPosition();
        
        // 躯干（国王长袍）
        renderer.drawRect(p.x - 10f, p.y - 10f, 20f, 30f, 0.8f, 0.2f, 0.2f, 1f);
        
        // 头部
        renderer.drawCircle(p.x, p.y - 20f, 8f, 24, 1.0f, 0.8f, 0.6f, 1.0f);
        
        // 王冠
        renderer.drawRect(p.x - 10f, p.y - 28f, 20f, 6f, 1.0f, 0.8f, 0.0f, 1.0f);
        
        // 眼睛
        renderer.drawCircle(p.x - 3.0f, p.y - 20.0f, 1.0f, 12, 0f, 0f, 0f, 1f);
        renderer.drawCircle(p.x + 3.0f, p.y - 20.0f, 1.0f, 12, 0f, 0f, 0f, 1f);
        
        // 嘴巴
        renderer.drawRect(p.x - 3.0f, p.y - 15.0f, 6.0f, 2.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        
        // 手臂（王袍的袖子）
        renderer.drawRect(p.x - 15f, p.y - 10f, 5f, 20f, 0.8f, 0.2f, 0.2f, 1f);
        renderer.drawRect(p.x + 10f, p.y - 10f, 5f, 20f, 0.8f, 0.2f, 0.2f, 1f);
        
        // 手部细节
        renderer.drawRect(p.x - 20f, p.y - 10f, 5f, 5f, 0.8f, 0.6f, 0.4f, 1f);
        renderer.drawRect(p.x + 15f, p.y - 10f, 5f, 5f, 0.8f, 0.6f, 0.4f, 1f);
        
        // 腿（王袍下的裤子）
        renderer.drawRect(p.x - 6f, p.y + 20f, 6f, 12f, 0.5f, 0.5f, 0.5f, 1f);
        renderer.drawRect(p.x + 0f, p.y + 20f, 6f, 12f, 0.5f, 0.5f, 0.5f, 1f);
        
        // 脚部细节
        renderer.drawRect(p.x - 6f, p.y + 32f, 6f, 3f, 0.3f, 0.3f, 0.3f, 1f);
        renderer.drawRect(p.x + 0f, p.y + 32f, 6f, 3f, 0.3f, 0.3f, 0.3f, 1f);
        renderer.drawRect(p.x - 6f, p.y + 35f, 3f, 2f, 0.2f, 0.2f, 0.2f, 1f);
        renderer.drawRect(p.x + 3f, p.y + 35f, 3f, 2f, 0.2f, 0.2f, 0.2f, 1f);
        
        // 渲染血条在王冠上方
        LifeFeatureComponent lifeFeature = getComponent(LifeFeatureComponent.class);
        if (lifeFeature != null) {
            int currentHealth = lifeFeature.getBlood();
            int maxHealth = 200; // 最大血量为200
            // 血条位于敌人王冠上方
            renderer.drawHealthBar(p.x - 20f, p.y - 38f, 40f, 5f, currentHealth, maxHealth);
        }
    }
    
    public void initAttackingSkills() {
        TransformComponent enemyKingTransform = getComponent(TransformComponent.class);
        Vector2 enemyKingPosition = enemyKingTransform.getPosition();
        
        for (int i = 0; i < 5; i++) {
            Vector2 position = new Vector2(
                enemyKingPosition.x + (random.nextFloat() - 0.5f) * 20,
                enemyKingPosition.y + (random.nextFloat() - 0.5f) * 20
            );
            
            EnemyAttackSkill attackingSkill = new EnemyAttackSkill(i, renderer, position, random);
            
            this.attackingSkills.add(attackingSkill);
            scene.addGameObject(attackingSkill);
        }
    }
    
    public List<GameObject> getAttackingSkills() {
        return this.attackingSkills;
    }
}
