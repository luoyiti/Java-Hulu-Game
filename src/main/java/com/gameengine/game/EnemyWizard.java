package com.gameengine.game;

import java.util.Random;

import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.RenderComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

public class EnemyWizard extends GameObject {
    private IRenderer renderer;
    private String imagePath = "";
    private Vector2 imageSize;
    
    public EnemyWizard(IRenderer renderer, Vector2 position, String imagePath, Vector2 imageSize, Random random) {

        super("EnemyWizard");
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

}