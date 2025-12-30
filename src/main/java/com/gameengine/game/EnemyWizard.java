package com.gameengine.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

/**
 * 敌人法师类
 * 中等难度敌人，使用陷阱球技能
 */
public class EnemyWizard extends GameObject {
    
    /**
     * 法师陷阱球技能内部类
     * 创新机制：陷阱球会静止漂浮在法师周围，当玩家接近时被激活并追踪玩家
     */
    private class AttackSkill extends GameObject {
        private static final String SKILL_IMAGE_PATH = "resources/picture/dirtball.png";
        private static final float ROTATION_EPSILON = 0.001f;
        private static final float ACTIVATION_RANGE = 150f; // 激活范围
        private static final float CHASE_SPEED = 180f; // 追击速度
        
        // 技能大小
        private final Vector2 skillSize = new Vector2(20, 20);
        private boolean isActivated = false; // 是否已被激活
        private Vector2 orbitCenter; // 环绕中心点
        private float orbitAngle; // 环绕角度
        private float orbitRadius; // 环绕半径
        private float orbitSpeed; // 环绕速度
        
        public AttackSkill(int index, Vector2 position, float angle, float radius) {
            super("EnemyWizard Trap Skill " + index);
            this.setEnemySkill();
            
            this.orbitCenter = new Vector2(position.x, position.y);
            this.orbitAngle = angle;
            this.orbitRadius = radius;
            this.orbitSpeed = 1.0f + random.nextFloat() * 0.5f; // 随机环绕速度
            
            this.addComponent(new TransformComponent(position));
            
            RenderComponent render = this.addComponent(new RenderComponent(SKILL_IMAGE_PATH, skillSize));
            render.setRenderer(renderer);
            
            PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.3f));
            physics.setVelocity(new Vector2(0, 0)); // 初始静止
            physics.setFriction(0.95f);
            
            // 添加生命周期组件，设置8秒生命周期（陷阱持续时间更长）
            LifeFeatureComponent lifeFeature = this.addComponent(new LifeFeatureComponent(1));
            lifeFeature.setLifetime(8.0f);
        }
        
        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            updateComponents(deltaTime);
            updateRotationFromVelocity();
        }
        
        /**
         * 检查并激活陷阱球（当玩家接近时）
         */
        public void checkActivation(Vector2 playerPosition) {
            if (isActivated || playerPosition == null) {
                return;
            }
            
            TransformComponent transform = getComponent(TransformComponent.class);
            if (transform == null) {
                return;
            }
            
            Vector2 skillPos = transform.getPosition();
            float distance = new Vector2(
                playerPosition.x - skillPos.x,
                playerPosition.y - skillPos.y
            ).magnitude();
            
            // 当玩家进入激活范围时，激活陷阱球
            if (distance < ACTIVATION_RANGE) {
                isActivated = true;
            }
        }
        
        /**
         * 更新陷阱球行为
         * 未激活：缓慢环绕法师
         * 已激活：快速追踪玩家
         */
        public void updateBehavior(Vector2 playerPosition, float deltaTime) {
            TransformComponent transform = getComponent(TransformComponent.class);
            PhysicsComponent physics = getComponent(PhysicsComponent.class);
            
            if (transform == null || physics == null) {
                return;
            }
            
            if (!isActivated) {
                // 未激活：缓慢环绕法师位置
                orbitAngle += orbitSpeed * deltaTime;
                float x = orbitCenter.x + (float)Math.cos(orbitAngle) * orbitRadius;
                float y = orbitCenter.y + (float)Math.sin(orbitAngle) * orbitRadius;
                transform.setPosition(new Vector2(x, y));
                physics.setVelocity(new Vector2(0, 0));
            } else if (playerPosition != null) {
                // 已激活：追踪玩家
                Vector2 skillPos = transform.getPosition();
                Vector2 direction = new Vector2(
                    playerPosition.x - skillPos.x,
                    playerPosition.y - skillPos.y
                );
                
                if (direction.magnitude() > 0) {
                    direction = direction.normalize();
                    physics.setVelocity(direction.multiply(CHASE_SPEED));
                }
            }
        }
        
        @Override
        public void render() {
            RenderComponent render = getComponent(RenderComponent.class);
            if (render != null) {
                // 激活后的陷阱球会自动旋转
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
            } else if (!isActivated) {
                // 未激活时缓慢自转
                render.setRotation(orbitAngle * 2);
            }
        }
        
        public boolean isActivated() {
            return isActivated;
        }
    }
    
    private IRenderer renderer;
    private Scene scene;
    private Random random;
    private List<GameObject> attackingSkills = new ArrayList<>();
    private String imagePath;
    private Vector2 imageSize;
    
    public EnemyWizard(IRenderer renderer, Scene scene, Vector2 position, String imagePath, Vector2 imageSize, Random random) {
        super("EnemyWizard");
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
     * 在法师周围创建4个陷阱球，呈环形分布
     */
    public void initAttackingSkills() {
        TransformComponent wizardTransform = getComponent(TransformComponent.class);
        Vector2 wizardPosition = wizardTransform.getPosition();
        
        int trapCount = 4; // 创建4个陷阱球
        float radius = 80f; // 环绕半径
        
        for (int i = 0; i < trapCount; i++) {
            float angle = (2 * (float)Math.PI / trapCount) * i;
            Vector2 position = new Vector2(
                wizardPosition.x + (float)Math.cos(angle) * radius,
                wizardPosition.y + (float)Math.sin(angle) * radius
            );
            
            // 使用内部类创建陷阱球技能
            AttackSkill attackingSkill = new AttackSkill(i, position, angle, radius);
            
            this.attackingSkills.add(attackingSkill);
            scene.addGameObject(attackingSkill);
        }
    }
    
    /**
     * 获取攻击技能列表
     */
    public List<GameObject> getAttackingSkills() {
        return this.attackingSkills;
    }
    
    /**
     * 更新陷阱球技能行为
     * 检查激活状态并更新行为
     */
    public void updateSkills(Vector2 playerPosition, float deltaTime) {
        if (attackingSkills.isEmpty()) {
            initAttackingSkills();
        }
        
        TransformComponent wizardTransform = getComponent(TransformComponent.class);
        if (wizardTransform != null) {
            Vector2 wizardPosition = wizardTransform.getPosition();
            
            for (GameObject skill : attackingSkills) {
                if (skill instanceof AttackSkill) {
                    AttackSkill trapSkill = (AttackSkill) skill;
                    // 更新环绕中心点为法师当前位置
                    trapSkill.orbitCenter = wizardPosition;
                    // 检查是否应该激活
                    trapSkill.checkActivation(playerPosition);
                    // 更新技能行为
                    trapSkill.updateBehavior(playerPosition, deltaTime);
                }
            }
        }
    }
    
    /**
     * 释放技能（重置陷阱球）
     * 与其他敌人不同，法师的技能是持续存在的陷阱
     */
    public boolean releaseAttackSkills() {
        // 法师的陷阱球会自动维护，只有在技能耗尽时才重新创建
        if (attackingSkills.isEmpty()) {
            initAttackingSkills();
            return true;
        }
        return false;
    }

}