package com.gameengine.app;
import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.scene.Scene;

public class OnlineScene extends Scene {

    private IRenderer renderer;
    private GameEngine engine;

    public OnlineScene(String name, GameEngine engine) {
        super(name);
        this.engine = engine;
        //TODO 实现网络游戏对战方法
    }

    @Override
    public void initialize() {
        this.renderer = engine.getRenderer();
    }

    @Override
    public void render() {
        
        // 绘制背景（基于图片）
        renderer.drawImage(
            "resources/picture/background1.png",
            0, 0,
            800, 600,
            1.0f
        );
        super.render();
    }
    
    @Override
    public void update(float deltaTime) {
        if (engine.getInputManager().isKeyJustPressed(256)) { // GLFW_KEY_ESCAPE（Esc 键）
            // 创建主菜单场景
            MenuScene menuScene = new MenuScene(engine, "MainMenu");
            engine.setScene(menuScene);
        }
    }
}
