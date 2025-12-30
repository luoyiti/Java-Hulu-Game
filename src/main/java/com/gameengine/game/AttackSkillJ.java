package com.gameengine.game;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

import java.util.Random;

/**
 * 玩家攻击技能J
 * 葫芦娃的技能，按J触发
 * 五行技能系统：金、木、水、火、土
 */
public class AttackSkillJ extends GameObject {
    
    private IRenderer renderer;
    private static final float ROTATION_EPSILON = 0.001f;
    
    /**
     * 五行技能类型枚举
     */
    public enum SkillType {
        METAL("金", "resources/picture/goldball.png"),   // 金 - 散射模式
        WOOD("木", "resources/picture/treeball.png"),    // 木 - 追踪模式
        WATER("水", "resources/picture/iceball.png"),    // 水 - 环绕模式
        FIRE("火", "resources/picture/fireball2.png"),   // 火 - 直线模式
        EARTH("土", "resources/picture/dirtball.png");   // 土 - 爆裂模式
        
        private final String chineseName;
        private final String imagePath;
        
        SkillType(String chineseName, String imagePath) {
            this.chineseName = chineseName;
            this.imagePath = imagePath;
        }
        
        public String getChineseName() {
            return chineseName;
        }
        
        public String getImagePath() {
            return imagePath;
        }
        
        /**
         * 随机获取一个技能类型
         */
        public static SkillType random(Random random) {
            SkillType[] types = values();
            return types[random.nextInt(types.length)];
        }
    }
    
    private SkillType skillType;
    private Vector2 skillSize;
    private float lifetime;
    private float orbitAngle; // 用于环绕模式
    private GameObject owner; // 技能所有者（玩家）
    private GameObject target; // 追踪目标
    
    // 默认技能图片路径（火焰）用于向后兼容
    private static final String DEFAULT_SKILL_IMAGE_PATH = "resources/picture/fireball2.png";
    private static final Vector2 DEFAULT_SKILL_SIZE = new Vector2(20, 20);
    
    /**
     * 原有构造函数，保持向后兼容，默认使用火焰技能
     */
    public AttackSkillJ(int index, IRenderer renderer, Random random) {
        this(index, renderer, random, SkillType.FIRE);
    }
    
    /**
     * 新构造函数，支持指定技能类型
     */
    public AttackSkillJ(int index, IRenderer renderer, Random random, SkillType skillType) {
        super("Attacking SkillJ " + index);
        this.renderer = renderer;
        this.skillType = skillType;
        this.setPlayerSkill();
        this.orbitAngle = 0;
        
        // 根据技能类型设置不同的大小和存活时间
        switch (skillType) {
            case METAL:
                this.skillSize = new Vector2(15, 15); // 金属弹较小
                this.lifetime = 0.8f;
                break;
            case WOOD:
                this.skillSize = new Vector2(18, 18);
                this.lifetime = 2.0f; // 追踪弹存活时间长
                break;
            case WATER:
                this.skillSize = new Vector2(16, 16);
                this.lifetime = 3.0f; // 环绕时间长
                break;
            case FIRE:
                this.skillSize = new Vector2(20, 20);
                this.lifetime = 1.0f;
                break;
            case EARTH:
                this.skillSize = new Vector2(25, 25); // 土球较大
                this.lifetime = 1.5f;
                break;
            default:
                this.skillSize = DEFAULT_SKILL_SIZE;
                this.lifetime = 1.0f;
        }
    }
    
    /**
     * 设置技能所有者
     */
    public void setOwner(GameObject owner) {
        this.owner = owner;
    }
    
    /**
     * 设置追踪目标
     */
    public void setTarget(GameObject target) {
        this.target = target;
    }
    
    /**
     * 获取技能类型
     */
    public SkillType getSkillType() {
        return skillType;
    }
    
    /**
     * 初始化技能位置和物理属性
     */
    public void initializePosition(Vector2 position, Random random) {
        this.addComponent(new TransformComponent(position));
        
        String imagePath = (skillType != null) ? skillType.getImagePath() : DEFAULT_SKILL_IMAGE_PATH;
        Vector2 size = (skillSize != null) ? skillSize : DEFAULT_SKILL_SIZE;
        
        RenderComponent render = this.addComponent(new RenderComponent(imagePath, size));
        render.setRenderer(renderer);
        
        PhysicsComponent physics = this.addComponent(new PhysicsComponent(0.5f));
        
        // 根据技能类型设置不同的初始速度
        switch (skillType) {
            case METAL:
                // 散射模式 - 随机方向高速发射
                physics.setVelocity(new Vector2(
                    (random.nextFloat() - 0.5f) * 400,
                    (random.nextFloat() - 0.5f) * 400
                ));
                physics.setFriction(0.99f);
                break;
            case WOOD:
                // 追踪模式 - 初始速度较慢
                physics.setVelocity(new Vector2(
                    (random.nextFloat() - 0.5f) * 100,
                    (random.nextFloat() - 0.5f) * 100
                ));
                physics.setFriction(1.0f); // 无摩擦
                break;
            case WATER:
                // 环绕模式 - 初始速度为零
                physics.setVelocity(new Vector2(0, 0));
                physics.setFriction(1.0f);
                break;
            case FIRE:
                // 直线模式 - 高速直线
                physics.setVelocity(new Vector2(
                    (random.nextFloat() - 0.5f) * 100,
                    (random.nextFloat() - 0.5f) * 100
                ));
                physics.setFriction(0.98f);
                break;
            case EARTH:
                // 爆裂模式 - 中速发射
                physics.setVelocity(new Vector2(
                    (random.nextFloat() - 0.5f) * 200,
                    (random.nextFloat() - 0.5f) * 200
                ));
                physics.setFriction(0.95f);
                break;
            default:
                physics.setVelocity(new Vector2(
                    (random.nextFloat() - 0.5f) * 100,
                    (random.nextFloat() - 0.5f) * 100
                ));
                physics.setFriction(0.98f);
        }
        
        // 添加生命周期组件
        LifeFeatureComponent lifeFeature = this.addComponent(new LifeFeatureComponent(1));
        lifeFeature.setLifetime(lifetime);
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateComponents(deltaTime);
        
        // 根据技能类型执行不同的更新逻辑
        if (skillType != null) {
            switch (skillType) {
                case WATER:
                    updateOrbitBehavior(deltaTime);
                    break;
                case WOOD:
                    updateTrackingBehavior(deltaTime);
                    break;
                default:
                    updateRotationFromVelocity();
                    break;
            }
        } else {
            updateRotationFromVelocity();
        }
    }
    
    /**
     * 环绕模式更新 - 水属性技能围绕玩家旋转
     */
    private void updateOrbitBehavior(float deltaTime) {
        if (owner == null) return;
        
        TransformComponent ownerTransform = owner.getComponent(TransformComponent.class);
        TransformComponent skillTransform = getComponent(TransformComponent.class);
        
        if (ownerTransform == null || skillTransform == null) return;
        
        // 更新环绕角度
        orbitAngle += deltaTime * 3.0f; // 旋转速度
        
        // 计算环绕位置
        float orbitRadius = 60.0f;
        Vector2 ownerPos = ownerTransform.getPosition();
        float newX = ownerPos.x + (float) Math.cos(orbitAngle) * orbitRadius;
        float newY = ownerPos.y + (float) Math.sin(orbitAngle) * orbitRadius;
        
        skillTransform.setPosition(new Vector2(newX, newY));
        
        // 更新旋转角度使其指向运动方向
        RenderComponent render = getComponent(RenderComponent.class);
        if (render != null) {
            render.setRotation(orbitAngle + (float) Math.PI / 2);
        }
    }
    
    /**
     * 追踪模式更新 - 木属性技能追踪敌人
     */
    private void updateTrackingBehavior(float deltaTime) {
        if (target == null || !target.isActive()) {
            updateRotationFromVelocity();
            return;
        }
        
        TransformComponent targetTransform = target.getComponent(TransformComponent.class);
        TransformComponent skillTransform = getComponent(TransformComponent.class);
        PhysicsComponent physics = getComponent(PhysicsComponent.class);
        
        if (targetTransform == null || skillTransform == null || physics == null) return;
        
        // 计算指向目标的方向
        Vector2 targetPos = targetTransform.getPosition();
        Vector2 skillPos = skillTransform.getPosition();
        Vector2 direction = new Vector2(targetPos.x - skillPos.x, targetPos.y - skillPos.y);
        
        if (direction.magnitude() > 0.1f) {
            direction = direction.normalize();
            // 缓慢转向目标
            Vector2 currentVel = physics.getVelocity();
            float speed = 200.0f;
            Vector2 targetVel = direction.multiply(speed);
            
            // 平滑插值
            float turnRate = 3.0f * deltaTime;
            Vector2 newVel = new Vector2(
                currentVel.x + (targetVel.x - currentVel.x) * turnRate,
                currentVel.y + (targetVel.y - currentVel.y) * turnRate
            );
            physics.setVelocity(newVel);
        }
        
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
    
    /**
     * 设置环绕角度（用于初始化多个环绕技能时错开位置）
     */
    public void setOrbitAngle(float angle) {
        this.orbitAngle = angle;
    }
}
