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
 * 敌人士兵类
 * 普通敌人单位
 */
public class EnemySoldier extends GameObject {
    
    /**
     * 士兵攻击技能内部类
     * 封装了冰球技能的所有行为
     */
    private class AttackSkill extends GameObject {
        private static final String SKILL_IMAGE_PATH = "resources/picture/iceball.png";
        private static final float ROTATION_EPSILON = 0.001f;
        // 技能大小
        private final Vector2 skillSize = new Vector2(20, 20);
        
        public AttackSkill(int index, Vector2 position) {
            super("EnemySoldier Attacking Skill " + index);
            this.setEnemySkill();
            
            this.addComponent(new TransformComponent(position));
            
            RenderComponent render = this.addComponent(new RenderComponent(SKILL_IMAGE_PATH, skillSize));
            render.setRenderer(renderer);
            
            PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.5f));
            physics.setVelocity(new Vector2(
                (random.nextFloat() - 0.5f) * 150,
                (random.nextFloat() - 0.5f) * 150
            ));
            physics.setFriction(0.98f);
            
            // 添加生命周期组件，设置3秒生命周期以延长移动距离
            LifeFeatureComponent lifeFeature = this.addComponent(new LifeFeatureComponent(1));
            lifeFeature.setLifetime(3.0f);
        }
        
        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            updateComponents(deltaTime);
            updateRotationFromVelocity();
        }
        
        @Override
        public void render() {
            RenderComponent render = getComponent(RenderComponent.class);
            if (render != null) {
                render.render();
            }
        }
        
        private void updateRotationFromVelocity() {
            PhysicsComponent physics = getComponent(PhysicsComponent.class);
            RenderComponent render = getComponent(RenderComponent.class);
            if (physics == null || render == null) {
                return;
            }

            Vector2 velocity = physics.getVelocity();
            if (velocity.magnitude() > ROTATION_EPSILON) {
                render.setRotation(velocity.angle());
            }
        }
    }
    
    private IRenderer renderer;
    private Scene scene;
    private Random random;
    private List<GameObject> attackingSkills = new ArrayList<>();
    private String imagePath;
    private Vector2 imageSize;
    
    public EnemySoldier(IRenderer renderer, Scene scene, Vector2 position, String imagePath, Vector2 imageSize, Random random) {
        super("EnemySoldier");
        this.renderer = renderer;
        this.scene = scene;
        this.random = random;
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
    
    /**
     * 初始化攻击技能
     * 创建1个冰球技能对象
     */
    public void initAttackingSkills() {
        TransformComponent soldierTransform = getComponent(TransformComponent.class);
        Vector2 soldierPosition = soldierTransform.getPosition();
        
        // 只创建1个冰球技能
        Vector2 position = new Vector2(soldierPosition.x, soldierPosition.y);
        // 使用内部类创建技能
        AttackSkill attackingSkill = new AttackSkill(0, position);
        
        this.attackingSkills.add(attackingSkill);
        scene.addGameObject(attackingSkill);
    }
    
    /**
     * 获取攻击技能列表
     */
    public List<GameObject> getAttackingSkills() {
        return this.attackingSkills;
    }

    /**
     * 释放士兵技能（向玩家方向发射冰球）
     * @param playerPosition 玩家位置
     */
    public boolean releaseAttackSkills(Vector2 playerPosition) {
        if (attackingSkills.isEmpty()) {
            initAttackingSkills();
        }

        if (attackingSkills.isEmpty() || playerPosition == null) {
            return false;
        }

        TransformComponent soldierTransform = getComponent(TransformComponent.class);
        if (soldierTransform == null) {
            return false;
        }

        Vector2 soldierPosition = soldierTransform.getPosition();
        boolean released = false;

        // 只发射一个冰球，向玩家方向
        if (!attackingSkills.isEmpty()) {
            GameObject attackingSkill = attackingSkills.get(0);
            TransformComponent skillTransform = attackingSkill.getComponent(TransformComponent.class);
            PhysicsComponent skillPhysics = attackingSkill.getComponent(PhysicsComponent.class);

            if (skillTransform != null && skillPhysics != null) {
                // 设置冰球起始位置为士兵位置
                skillTransform.setPosition(new Vector2(soldierPosition.x, soldierPosition.y));

                // 计算朝向玩家的方向向量
                Vector2 direction = new Vector2(
                    playerPosition.x - soldierPosition.x,
                    playerPosition.y - soldierPosition.y
                );
                
                if (direction.magnitude() > 0) {
                    direction = direction.normalize();
                    // 设置更高的速度以延长移动距离
                    skillPhysics.setVelocity(direction.multiply(250));
                    // 降低摩擦力以延长移动距离
                    skillPhysics.setFriction(0.99f);
                }

                LifeFeatureComponent lifeFeature = attackingSkill.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.resetLifetime();
                }

                released = true;
            }
        }

        return released;
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
