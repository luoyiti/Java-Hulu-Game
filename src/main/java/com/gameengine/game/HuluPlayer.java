package com.gameengine.game;

import com.gameengine.app.OnlineGameScene.Status;
import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 葫芦娃玩家类
 * 作为游戏主角，可以通过GameLogic中的规则操控
 * 使用 PNG 图片进行渲染
 */

public class HuluPlayer extends GameObject {
    
    private Vector2 basePosition;
    private List<GameObject> attackingSkillsJ = new ArrayList<>();
    private IRenderer renderer;
    private Scene scene;
    private Random random;

    private static int huluCount = 0;
    private int imageIndex = 0;

    private Status status = null; // 默认为null，仅在网络游戏中赋值
    private String addressId = null; // 默认为null，仅在网络游戏中表示客户端的地址
    
    // 当前五行技能类型
    private AttackSkillJ.SkillType currentSkillType;
    
    // 图片渲染相关 - 七个葫芦娃兄弟的图片
    // 大娃(红)、二娃(橙)、三娃(黄)、四娃(绿)、五娃(青)、六娃(蓝)、七娃(紫)
    private static final String[] IMAGE_PATH = {
        "resources/picture/huluBro1.png",  // 大娃 - 力大无穷
        "resources/picture/huluBro2.png",  // 二娃 - 千里眼顺风耳
        "resources/picture/huluBro3.png",  // 三娃 - 铜头铁臂
        "resources/picture/huluBro4.png",  // 四娃 - 喷火
        "resources/picture/huluBro5.png",  // 五娃 - 吐水
        "resources/picture/huluBro6.png",  // 六娃 - 隐身
        "resources/picture/huluBro7.png"   // 七娃 - 宝葫芦
    };

    private static final float IMAGE_WIDTH = 40f;   // 图片渲染宽度
    private static final float IMAGE_HEIGHT = 50f;  // 图片渲染高度
    
    // 无敌帧闪烁相关
    private float blinkTimer; // 闪烁计时器
    private float blinkInterval = 0.1f; // 闪烁间隔（秒）
    
    public HuluPlayer(IRenderer renderer, Scene scene) {
        super("Hulu Player");
        this.renderer = renderer;
        this.scene = scene;
        this.random = new Random();
        this.setPlayer();
        // assign a per-instance image index and advance the global counter
        this.imageIndex = huluCount % IMAGE_PATH.length;
        huluCount++;

        // 添加变换组件（玩家初始位置在地图中心：800x600的中心）
        this.addComponent(new TransformComponent(new Vector2(400, 300)));
        
        // 添加物理组件
        PhysicsComponent physics = this.addComponent(new PhysicsComponent(1.0f));
        physics.setFriction(0.95f);
        
        // 添加生命特征组件
        this.addComponent(new LifeFeatureComponent(100));
        
        // 添加图片渲染组件
        RenderComponent render = this.addComponent(new RenderComponent(
            IMAGE_PATH[imageIndex], 
            new Vector2(IMAGE_WIDTH, IMAGE_HEIGHT)
        ));
        render.setRenderer(renderer);
        
        // 初始化闪烁计时器
        this.blinkTimer = 0;
    }

    /**
     * 为本地多人游戏设定的构造函数，允许自定义玩家名称
     */
    public HuluPlayer(IRenderer renderer, Scene scene, String name) {
        this(renderer, scene);
        this.setName(name);
    }
    
    /**
     * 为网络多人游戏设定的构造函数，允许自定义玩家名称
     */
    public HuluPlayer(IRenderer renderer, Scene scene, String name, Status status) {
        this(renderer, scene);
        this.setName(name);
        this.status = status;
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateComponents(deltaTime);
        updateBodyParts();
        
        // 更新闪烁计时器
        blinkTimer += deltaTime;
        
        if (attackingSkillsJ.isEmpty()) {
            initAttackSkillJ();
        }
    }
    
    @Override
    public void render() {
        renderWithImage();
    }
    
    private void updateBodyParts() {
        TransformComponent transform = getComponent(TransformComponent.class);
        if (transform != null) {
            basePosition = transform.getPosition();
        }
    }
    
    /**
     * 使用图片渲染葫芦娃
     */
    private void renderWithImage() {
        if (basePosition == null) return;
        
        // 检查是否处于无敌帧状态
        LifeFeatureComponent lifeComponent = getComponent(LifeFeatureComponent.class);
        if (lifeComponent != null && lifeComponent.isInvincible()) {
            // 计算当前闪烁周期
            int blinkCycle = (int)(blinkTimer / blinkInterval);
            
            // 每两个周期闪烁一次（显示/隐藏交替）
            if (blinkCycle % 2 != 0) {
                return; // 不渲染，实现闪烁效果
            }
        }
        
        Vector2 screenPosition = basePosition;
        
        // 如果存在相机，将世界坐标转换为屏幕坐标
        if (scene != null && scene.getCamera() != null) {
            com.gameengine.core.Camera camera = scene.getCamera();
            
            // // 视锥剔除：只渲染可见的对象
            // if (!camera.isVisible(basePosition, IMAGE_WIDTH, IMAGE_HEIGHT)) {
            //     return;
            // }
            
            // 转换为屏幕坐标
            screenPosition = camera.worldToScreen(basePosition);
        }
        
        // 使用图片渲染（图片中心对齐到位置点）
        renderer.drawImage(
            IMAGE_PATH[imageIndex],
            screenPosition.x - IMAGE_WIDTH / 2,
            screenPosition.y - IMAGE_HEIGHT / 2,
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            1.0f
        );
    }
    
    /**
     * 初始化技能J
     * 葫芦娃的五行技能，按J触发
     * 每次初始化随机选择金木水火土五种技能之一
     */
    public void initAttackSkillJ() {
        // 清除旧技能 - 将旧技能设置为非活跃状态，Scene会在update时自动移除
        for (GameObject oldSkill : attackingSkillsJ) {
            oldSkill.setActive(false);
        }
        attackingSkillsJ.clear();
        
        // 每次初始化技能时随机选择一种五行技能
        currentSkillType = AttackSkillJ.SkillType.random(random);
        
        int skillCount = getSkillCountForType(currentSkillType);
        
        for (int i = 0; i < skillCount; i++) {
            AttackSkillJ attackingSkillJ = new AttackSkillJ(i, renderer, random, currentSkillType);
            attackingSkillJ.setOwner(this);
            
            TransformComponent attackingSkillJTransform = getComponent(TransformComponent.class);
            Vector2 attackingSkillJTransformPosition = attackingSkillJTransform.getPosition();
            
            Vector2 position = new Vector2(
                attackingSkillJTransformPosition.x + (random.nextFloat() - 0.5f) * 20,
                attackingSkillJTransformPosition.y + (random.nextFloat() - 0.5f) * 20
            );
            
            attackingSkillJ.initializePosition(position, random);
            
            // 为水属性技能设置环绕角度，错开位置
            if (currentSkillType == AttackSkillJ.SkillType.WATER) {
                attackingSkillJ.setOrbitAngle((float)(i * 2 * Math.PI / skillCount));
            }
            
            this.attackingSkillsJ.add(attackingSkillJ);
            scene.addGameObject(attackingSkillJ);
        }
    }
    
    /**
     * 根据技能类型返回不同的技能数量
     */
    private int getSkillCountForType(AttackSkillJ.SkillType type) {
        switch (type) {
            case METAL: return 5;  // 金 - 散射5个
            case WOOD: return 2;   // 木 - 追踪2个
            case WATER: return 4;  // 水 - 环绕4个
            case FIRE: return 2;   // 火 - 直线2个
            case EARTH: return 3;  // 土 - 爆裂3个
            default: return 2;
        }
    }
    
    /**
     * 获取当前五行技能类型
     */
    public AttackSkillJ.SkillType getCurrentSkillType() {
        return currentSkillType;
    }
    
    public List<GameObject> getAttackingSkillsJ() {
        return this.attackingSkillsJ;
    }

    /**
     * 释放技能J（由玩家自身完成技能释放逻辑）
     * 根据当前五行技能类型，使用不同的释放模式
     */
    public boolean releaseSkillJ() {
        if (attackingSkillsJ.isEmpty()) {
            initAttackSkillJ();
        }

        if (attackingSkillsJ.isEmpty()) {
            return false;
        }

        TransformComponent playerTransform = getComponent(TransformComponent.class);
        if (playerTransform == null) {
            return false;
        }

        Vector2 playerPosition = playerTransform.getPosition();
        float angleStep = (2 * (float) Math.PI) / attackingSkillsJ.size();
        boolean released = false;

        for (int i = 0; i < attackingSkillsJ.size(); i++) {
            GameObject skill = attackingSkillsJ.get(i);
            TransformComponent skillTransform = skill.getComponent(TransformComponent.class);
            PhysicsComponent skillPhysics = skill.getComponent(PhysicsComponent.class);

            if (skillTransform != null && skillPhysics != null) {
                skillTransform.setPosition(new Vector2(playerPosition.x, playerPosition.y));

                // 根据技能类型设置不同的释放速度
                float speed = getSkillSpeedForType(currentSkillType);
                float angle = i * angleStep;
                Vector2 direction = new Vector2((float) Math.cos(angle), (float) Math.sin(angle));
                if (direction.magnitude() > 0) {
                    direction = direction.normalize();
                }

                // 水属性技能不需要设置速度（环绕模式）
                if (currentSkillType != AttackSkillJ.SkillType.WATER) {
                    skillPhysics.setVelocity(direction.multiply(speed));
                }
                
                skill.setActive(true);

                LifeFeatureComponent lifeFeature = skill.getComponent(LifeFeatureComponent.class);
                if (lifeFeature != null) {
                    lifeFeature.resetLifetime();
                }

                released = true;
            }
        }

        return released;
    }
    
    /**
     * 根据技能类型返回不同的释放速度
     */
    private float getSkillSpeedForType(AttackSkillJ.SkillType type) {
        if (type == null) return 300f;
        switch (type) {
            case METAL: return 400f;  // 金 - 高速散射
            case WOOD: return 200f;   // 木 - 中速追踪
            case WATER: return 0f;    // 水 - 环绕不需要速度
            case FIRE: return 350f;   // 火 - 高速直线
            case EARTH: return 250f;  // 土 - 中速爆裂
            default: return 300f;
        }
    }

    public boolean hasRemoteAddress() {
        return this.addressId != null;
    }

    public void setRemoteAddress(String address) {
        this.addressId = address;
    }
    
    public String getRemoteAddress() {
        return this.addressId;
    }

    public Status getStatus() {
        return this.status;
    }
    
    /**
     * 根据关卡设置葫芦娃形象
     * @param level 关卡号（1-7对应大娃到七娃）
     */
    public void setImageForLevel(int level) {
        // 关卡1-7对应imageIndex 0-6
        int newIndex = Math.max(0, Math.min(level - 1, IMAGE_PATH.length - 1));
        if (newIndex != this.imageIndex) {
            this.imageIndex = newIndex;
            // 更新渲染组件的图片路径
            RenderComponent render = getComponent(RenderComponent.class);
            if (render != null) {
                render.setImagePath(IMAGE_PATH[imageIndex]);
            }
        }
    }
    
    /**
     * 获取当前使用的葫芦娃图片索引
     */
    public int getImageIndex() {
        return this.imageIndex;
    }
    
    /**
     * 获取葫芦娃的名称（根据当前形象）
     */
    public String getHuluName() {
        String[] names = {"大娃", "二娃", "三娃", "四娃", "五娃", "六娃", "七娃"};
        if (imageIndex >= 0 && imageIndex < names.length) {
            return names[imageIndex];
        }
        return "葫芦娃";
    }
    
    /**
     * 原始手绘渲染方法（保留作为备用）
     */
    // private void renderBodyPartsLegacy() {
    //     if (basePosition == null) return;
    //     
    //     // 渲染葫芦身体上部 - 较小的圆
    //     renderer.drawCircle(
    //         basePosition.x, basePosition.y - 8, 6.0f, 32,
    //         1.0f, 0.0f, 0.0f, 1.0f  // 红色
    //     );
    //     
    //     // 头顶小叶子（绿色叶片 + 茎）
    //     renderer.drawRect(
    //         basePosition.x - 0.75f, basePosition.y - 20.0f, 1.5f, 5.0f,
    //         0.10f, 0.45f, 0.10f, 1.0f  // 绿色茎
    //     );
    //     
    //     renderer.drawCircle(
    //         basePosition.x + 3.0f, basePosition.y - 20.0f, 3.4f, 27,
    //         0.15f, 0.70f, 0.20f, 1.0f  // 右叶片
    //     );
    //     
    //     // 渲染眼睛（白色眼白 + 黑色瞳孔）
    //     renderer.drawCircle(
    //         basePosition.x - 3.0f, basePosition.y - 10.0f, 1.8f, 16,
    //         1.0f, 1.0f, 1.0f, 1.0f  // 白色
    //     );
    //     renderer.drawCircle(
    //         basePosition.x + 3.0f, basePosition.y - 10.0f, 1.8f, 16,
    //         1.0f, 1.0f, 1.0f, 1.0f  // 白色
    //     );
    //     renderer.drawCircle(
    //         basePosition.x - 3.0f, basePosition.y - 10.0f, 0.8f, 12,
    //         0.0f, 0.0f, 0.0f, 1.0f  // 黑色瞳孔
    //     );
    //     renderer.drawCircle(
    //         basePosition.x + 3.0f, basePosition.y - 10.0f, 0.8f, 12,
    //         0.0f, 0.0f, 0.0f, 1.0f  // 黑色瞳孔
    //     );
    //     
    //     // 渲染嘴巴（细长矩形）
    //     renderer.drawRect(
    //         basePosition.x - 2.0f, basePosition.y - 7.0f, 4.0f, 2.0f,
    //         0.0f, 0.0f, 0.0f, 1.0f  // 黑色
    //     );
    //     
    //     // 渲染葫芦身体下部 - 较大的圆
    //     renderer.drawCircle(
    //         basePosition.x, basePosition.y + 5, 10.0f, 32,
    //         1.0f, 0.0f, 0.0f, 1.0f  // 红色
    //     );
    // }
}
