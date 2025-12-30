package com.gameengine.game;

import com.gameengine.components.*;
import com.gameengine.core.GameObject;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

/**
 * 树木装饰类
 * 场景中的装饰物
 */
public class Tree extends GameObject {
    
    private IRenderer renderer;
    
    public Tree(IRenderer renderer, Vector2 position) {
        super("tree");
        this.renderer = renderer;
        
        this.addComponent(new TransformComponent(position));
        
        RenderComponent render = this.addComponent(new RenderComponent(
            RenderComponent.RenderType.CIRCLE,
            new Vector2(5, 5),
            new RenderComponent.Color(0.5f, 0.5f, 1.0f, 0.8f)
        ));
        render.setRenderer(renderer);
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
        
        Vector2 worldPos = transform.getPosition();
        Vector2 screenPos = worldPos;
        
        // 如果存在相机，将世界坐标转换为屏幕坐标
        if (getScene() != null && getScene().getCamera() != null) {
            com.gameengine.core.Camera camera = getScene().getCamera();
            
            // 视锥剔除：只渲染可见的树木
            if (!camera.isVisible(worldPos, 16, 16)) {
                return;
            }
            
            // 转换为屏幕坐标
            screenPos = camera.worldToScreen(worldPos);
        }
        
        // 渲染树干
        renderer.drawRect(screenPos.x - 1.0f, screenPos.y, 2.0f, 10.0f, 0.54f, 0.27f, 0.07f, 1.0f);
        
        // 渲染树冠（顶部）
        renderer.drawCircle(screenPos.x, screenPos.y - 5.0f, 8.0f, 32, 0.0f, 0.5f, 0.0f, 1.0f);
    }
}
