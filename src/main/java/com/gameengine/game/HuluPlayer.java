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
    
    // 图片渲染相关
    private static final String IMAGE_PATH = "resources/picture/huluBro1.png";
    private static final float IMAGE_WIDTH = 40f;   // 图片渲染宽度
    private static final float IMAGE_HEIGHT = 50f;  // 图片渲染高度
    
    public HuluPlayer(IRenderer renderer, Scene scene) {
        super("Hulu Player");
        this.renderer = renderer;
        this.scene = scene;
        this.random = new Random();
        this.setPlayer();
        
        // 添加变换组件（玩家初始位置在地图中心：800x600的中心）
        this.addComponent(new TransformComponent(new Vector2(400, 300)));
        
        // 添加物理组件
        PhysicsComponent physics = this.addComponent(new PhysicsComponent(1.0f));
        physics.setFriction(0.95f);
        
        // 添加生命特征组件
        this.addComponent(new LifeFeatureComponent(100));
        
        // 添加图片渲染组件
        RenderComponent render = this.addComponent(new RenderComponent(
            IMAGE_PATH, 
            new Vector2(IMAGE_WIDTH, IMAGE_HEIGHT)
        ));
        render.setRenderer(renderer);
    }
    
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        updateComponents(deltaTime);
        updateBodyParts();
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
        
        // 使用图片渲染（图片中心对齐到位置点）
        renderer.drawImage(
            IMAGE_PATH,
            basePosition.x - IMAGE_WIDTH / 2,
            basePosition.y - IMAGE_HEIGHT / 2,
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            1.0f
        );
    }
    
    /**
     * 初始化技能J
     * 葫芦娃的技能1，按J触发
     * 可以向左右方向发射两道光束，造成10伤害
     */
    public void initAttackSkillJ() {
        for (int i = 0; i < 2; i++) {
            AttackSkillJ attackingSkillJ = new AttackSkillJ(i, renderer, random);
            
            TransformComponent attackingSkillJTransform = getComponent(TransformComponent.class);
            Vector2 attackingSkillJTransformPosition = attackingSkillJTransform.getPosition();
            
            Vector2 position = new Vector2(
                attackingSkillJTransformPosition.x + (random.nextFloat() - 0.5f) * 20,
                attackingSkillJTransformPosition.y + (random.nextFloat() - 0.5f) * 20
            );
            
            attackingSkillJ.initializePosition(position, random);
            
            this.attackingSkillsJ.add(attackingSkillJ);
            scene.addGameObject(attackingSkillJ);
        }
    }
    
    public List<GameObject> getAttackingSkillsJ() {
        return this.attackingSkillsJ;
    }
    
    /**
     * 原始手绘渲染方法（保留作为备用）
     */
    // private void renderBodyPartsLegacy() {
    //     if (basePosition == null) return;
        
    //     // 渲染葫芦身体上部 - 较小的圆
    //     renderer.drawCircle(
    //         basePosition.x, basePosition.y - 8, 6.0f, 32,
    //         1.0f, 0.0f, 0.0f, 1.0f  // 红色
    //     );
        
    //     // 头顶小叶子（绿色叶片 + 茎）
    //     renderer.drawRect(
    //         basePosition.x - 0.75f, basePosition.y - 20.0f, 1.5f, 5.0f,
    //         0.10f, 0.45f, 0.10f, 1.0f  // 绿色茎
    //     );
        
    //     renderer.drawCircle(
    //         basePosition.x + 3.0f, basePosition.y - 20.0f, 3.4f, 27,
    //         0.15f, 0.70f, 0.20f, 1.0f  // 右叶片
    //     );
        
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
        
    //     // 渲染嘴巴（细长矩形）
    //     renderer.drawRect(
    //         basePosition.x - 2.0f, basePosition.y - 7.0f, 4.0f, 2.0f,
    //         0.0f, 0.0f, 0.0f, 1.0f  // 黑色
    //     );
        
    //     // 渲染葫芦身体下部 - 较大的圆
    //     renderer.drawCircle(
    //         basePosition.x, basePosition.y + 5, 10.0f, 32,
    //         1.0f, 0.0f, 0.0f, 1.0f  // 红色
    //     );
    // }
}
