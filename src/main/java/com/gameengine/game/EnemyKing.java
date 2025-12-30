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
 * 终极BOSS - 多阶段战斗系统
 */
public class EnemyKing extends GameObject {
    
    /**
     * BOSS战斗阶段枚举
     */
    private enum BattlePhase {
        PHASE_1, // 第一阶段：螺旋弹幕（100%-60%血量）
        PHASE_2, // 第二阶段：追踪火球（60%-30%血量）
        PHASE_3  // 第三阶段：狂暴模式（30%-0%血量）
    }
    
    /**
     * 国王攻击技能内部类
     * 支持多种攻击模式的智能火球
     */
    private class AttackSkill extends GameObject {
        private static final String SKILL_IMAGE_PATH = "resources/picture/fireball.png";
        private static final float ROTATION_EPSILON = 0.001f;
        private static final float CHASE_SPEED = 200f; // 追踪速度
        
        // 技能大小
        private final Vector2 skillSize = new Vector2(20, 20);
        private boolean isChasing = false; // 是否为追踪模式
        private float accelerationTimer = 0f; // 加速计时器
        
        public AttackSkill(int index, Vector2 position, boolean chasing) {
            super("King Skill " + index);
            this.setEnemySkill();
            this.isChasing = chasing;
            
            this.addComponent(new TransformComponent(position));
            
            RenderComponent render = this.addComponent(new RenderComponent(SKILL_IMAGE_PATH, skillSize));
            render.setRenderer(renderer);
            
            PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.5f));
            physics.setVelocity(new Vector2(
                (random.nextFloat() - 0.5f) * 100,
                (random.nextFloat() - 0.5f) * 100
            ));
            physics.setFriction(isChasing ? 0.99f : 0.98f);
            
            // 追踪火球生命周期更长
            float lifetime = isChasing ? 4.0f : 2.5f;
            LifeFeatureComponent lifeFeature = this.addComponent(new LifeFeatureComponent(1));
            lifeFeature.setLifetime(lifetime);
        }
        
        @Override
        public void update(float deltaTime) {
            super.update(deltaTime);
            updateComponents(deltaTime);
            updateRotationFromVelocity();
            
            // 追踪模式的特殊行为
            if (isChasing) {
                accelerationTimer += deltaTime;
                // 每0.2秒加速一次
                if (accelerationTimer >= 0.2f) {
                    accelerationTimer = 0f;
                }
            }
        }
        
        /**
         * 更新追踪行为
         */
        public void updateChasing(Vector2 playerPosition) {
            if (!isChasing || playerPosition == null) {
                return;
            }
            
            TransformComponent transform = getComponent(TransformComponent.class);
            PhysicsComponent physics = getComponent(PhysicsComponent.class);
            
            if (transform == null || physics == null) {
                return;
            }
            
            Vector2 skillPos = transform.getPosition();
            Vector2 direction = new Vector2(
                playerPosition.x - skillPos.x,
                playerPosition.y - skillPos.y
            );
            
            if (direction.magnitude() > 0) {
                direction = direction.normalize();
                // 渐进式加速
                float currentSpeed = CHASE_SPEED * (1.0f + accelerationTimer);
                physics.setVelocity(direction.multiply(currentSpeed));
            }
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
        
        public boolean isChasing() {
            return isChasing;
        }
    }
    
    private IRenderer renderer;
    private Scene scene;
    private Random random;
    private List<GameObject> attackingSkills = new ArrayList<>();
    private String imagePath;
    private Vector2 imageSize;
    
    // BOSS战斗系统
    private BattlePhase currentPhase = BattlePhase.PHASE_1;
    private float spiralAngle = 0f; // 螺旋弹幕的当前角度
    private int phaseSkillCount = 0; // 当前阶段已释放的技能数量
    private boolean hasEnteredPhase2 = false;
    private boolean hasEnteredPhase3 = false;
    
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
    
    /**
     * 初始化攻击技能（根据阶段创建不同数量和类型的技能）
     */
    public void initAttackingSkills() {
        TransformComponent transform = getComponent(TransformComponent.class);
        if (transform == null) return;
        
        Vector2 kingPosition = transform.getPosition();
        int skillCount = getSkillCountForPhase();
        
        attackingSkills.clear();
        
        for (int i = 0; i < skillCount; i++) {
            Vector2 position = new Vector2(kingPosition.x, kingPosition.y);
            boolean isChasing = currentPhase != BattlePhase.PHASE_1;
            
            AttackSkill skill = new AttackSkill(i, position, isChasing);
            attackingSkills.add(skill);
            scene.addGameObject(skill);
        }
    }
    
    /**
     * 获取当前阶段应该创建的技能数量
     */
    private int getSkillCountForPhase() {
        switch (currentPhase) {
            case PHASE_1: return 8;  // 第一阶段：8个火球
            case PHASE_2: return 12; // 第二阶段：12个火球
            case PHASE_3: return 16; // 第三阶段：16个火球（狂暴）
            default: return 8;
        }
    }
    
    /**
     * 更新战斗阶段
     */
    private void updateBattlePhase() {
        LifeFeatureComponent lifeFeature = getComponent(LifeFeatureComponent.class);
        if (lifeFeature == null) return;
        
        int currentHealth = lifeFeature.getBlood();
        int maxHealth = 200;
        float healthPercent = (float) currentHealth / maxHealth;
        
        // 阶段转换
        if (healthPercent <= 0.3f && !hasEnteredPhase3) {
            currentPhase = BattlePhase.PHASE_3;
            hasEnteredPhase3 = true;
            // 进入狂暴模式，重新初始化技能
            attackingSkills.clear();
            System.out.println("BOSS进入第三阶段：狂暴模式！");
        } else if (healthPercent <= 0.6f && !hasEnteredPhase2) {
            currentPhase = BattlePhase.PHASE_2;
            hasEnteredPhase2 = true;
            attackingSkills.clear();
            System.out.println("BOSS进入第二阶段：追踪模式！");
        }
    }
    
    /**
     * 更新追踪火球
     */
    public void updateSkills(Vector2 playerPosition, float deltaTime) {
        for (GameObject skill : attackingSkills) {
            if (skill instanceof AttackSkill) {
                AttackSkill attackSkill = (AttackSkill) skill;
                if (attackSkill.isChasing()) {
                    attackSkill.updateChasing(playerPosition);
                }
            }
        }
    }
    
    public List<GameObject> getAttackingSkills() {
        return this.attackingSkills;
    }

    /**
     * 多阶段技能释放系统
     * 根据当前战斗阶段释放不同模式的技能
     */
    public boolean releaseAttackSkills() {
        // 更新战斗阶段
        updateBattlePhase();
        
        if (attackingSkills.isEmpty()) {
            initAttackingSkills();
        }

        if (attackingSkills.isEmpty()) {
            return false;
        }

        TransformComponent kingTransform = getComponent(TransformComponent.class);
        if (kingTransform == null) {
            return false;
        }

        Vector2 kingPosition = kingTransform.getPosition();
        
        // 根据阶段选择技能释放模式
        switch (currentPhase) {
            case PHASE_1:
                return releaseSpiralPattern(kingPosition);
            case PHASE_2:
                return releaseChasingPattern(kingPosition);
            case PHASE_3:
                return releaseBerserkPattern(kingPosition);
            default:
                return false;
        }
    }
    
    /**
     * 第一阶段：螺旋弹幕模式
     * 火球以螺旋方式持续发射
     */
    private boolean releaseSpiralPattern(Vector2 kingPosition) {
        float baseSpeed = 250f;
        boolean released = false;
        
        for (int i = 0; i < attackingSkills.size(); i++) {
            GameObject skill = attackingSkills.get(i);
            TransformComponent skillTransform = skill.getComponent(TransformComponent.class);
            PhysicsComponent skillPhysics = skill.getComponent(PhysicsComponent.class);
            
            if (skillTransform != null && skillPhysics != null) {
                skillTransform.setPosition(new Vector2(kingPosition.x, kingPosition.y));
                
                // 螺旋角度：每个火球基于总角度 + 均匀分布
                float angle = spiralAngle + (2 * (float)Math.PI / attackingSkills.size()) * i;
                Vector2 direction = new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
                
                if (direction.magnitude() > 0) {
                    direction = direction.normalize();
                }
                
                // 不同的火球有不同的速度，形成螺旋扩散效果
                float speed = baseSpeed + (i % 3) * 30f;
                skillPhysics.setVelocity(direction.multiply(speed));
                
                LifeFeatureComponent lifeFeature = skill.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.resetLifetime();
                }
                
                released = true;
            }
        }
        
        // 更新螺旋角度，形成旋转效果
        spiralAngle += 0.3f;
        if (spiralAngle >= 2 * Math.PI) {
            spiralAngle -= 2 * (float)Math.PI;
        }
        
        return released;
    }
    
    /**
     * 第二阶段：追踪火球模式
     * 部分火球追踪玩家，部分螺旋发射
     */
    private boolean releaseChasingPattern(Vector2 kingPosition) {
        float baseSpeed = 280f;
        boolean released = false;
        
        for (int i = 0; i < attackingSkills.size(); i++) {
            GameObject skill = attackingSkills.get(i);
            TransformComponent skillTransform = skill.getComponent(TransformComponent.class);
            PhysicsComponent skillPhysics = skill.getComponent(PhysicsComponent.class);
            
            if (skillTransform != null && skillPhysics != null) {
                skillTransform.setPosition(new Vector2(kingPosition.x, kingPosition.y));
                
                // 追踪火球初始速度随机
                Vector2 direction;
                if (i % 2 == 0) {
                    // 偶数：螺旋发射
                    float angle = spiralAngle + (2 * (float)Math.PI / attackingSkills.size()) * i;
                    direction = new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
                } else {
                    // 奇数：随机方向（之后会追踪）
                    float angle = random.nextFloat() * 2 * (float)Math.PI;
                    direction = new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
                }
                
                if (direction.magnitude() > 0) {
                    direction = direction.normalize();
                }
                
                skillPhysics.setVelocity(direction.multiply(baseSpeed));
                
                LifeFeatureComponent lifeFeature = skill.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.resetLifetime();
                }
                
                released = true;
            }
        }
        
        spiralAngle += 0.4f;
        return released;
    }
    
    /**
     * 第三阶段：狂暴模式
     * 密集弹幕 + 全部追踪 + 高速
     */
    private boolean releaseBerserkPattern(Vector2 kingPosition) {
        float baseSpeed = 320f; // 更高的初始速度
        boolean released = false;
        
        for (int i = 0; i < attackingSkills.size(); i++) {
            GameObject skill = attackingSkills.get(i);
            TransformComponent skillTransform = skill.getComponent(TransformComponent.class);
            PhysicsComponent skillPhysics = skill.getComponent(PhysicsComponent.class);
            
            if (skillTransform != null && skillPhysics != null) {
                skillTransform.setPosition(new Vector2(kingPosition.x, kingPosition.y));
                
                // 狂暴模式：密集的全方向发射
                float angle = (2 * (float)Math.PI / attackingSkills.size()) * i + spiralAngle;
                Vector2 direction = new Vector2((float)Math.cos(angle), (float)Math.sin(angle));
                
                if (direction.magnitude() > 0) {
                    direction = direction.normalize();
                }
                
                // 狂暴模式速度更快且有随机性
                float speed = baseSpeed + random.nextFloat() * 80f;
                skillPhysics.setVelocity(direction.multiply(speed));
                
                LifeFeatureComponent lifeFeature = skill.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.resetLifetime();
                }
                
                released = true;
            }
        }
        
        // 狂暴模式螺旋速度更快
        spiralAngle += 0.6f;
        return released;
    }
}
