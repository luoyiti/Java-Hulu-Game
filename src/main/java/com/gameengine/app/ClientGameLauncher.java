package com.gameengine.app;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.RenderBackend;

public class ClientGameLauncher {

    public static void main(String[] args) {
        GameEngine engine = null;
        try {
            System.out.println("使用渲染后端: GPU");
            // 初始化游戏引擎（800x600分辨率）
            engine = new GameEngine(800, 600, "葫芦娃大战妖怪", RenderBackend.GPU);

            OnlineGameScene onlineGameScene = new OnlineGameScene("OnlineGame", engine, 2,
                    OnlineGameScene.Status.CLIENT);
            engine.setScene(onlineGameScene);

            // 运行游戏
            engine.run();
        } catch (Exception e) {
            System.err.println("游戏运行出错: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
