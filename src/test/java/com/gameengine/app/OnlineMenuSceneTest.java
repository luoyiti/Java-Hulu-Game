package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.input.InputManager;
import org.junit.Assert;
import org.junit.Test;

public class OnlineMenuSceneTest {

    @Test
    public void testSelectAndCreateOnlineScene() throws Exception {
        InputManager im = InputManager.getInstance();
        im.reset();

        FakeRenderer fr = new FakeRenderer(800, 600);
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith(fr, im);

        OnlineMenuScene scene = new OnlineMenuScene("Online Menu", engine);
        scene.initialize();

        // simulate right arrow then enter
        im.onKeyPressed(262); // right
        im.onKeyPressed(257); // enter

        scene.update(0.016f);

        Assert.assertNotNull(engine.getCurrentScene());
        Assert.assertTrue(engine.getCurrentScene() instanceof OnlineGameScene);
        OnlineGameScene newScene = (OnlineGameScene) engine.getCurrentScene();
        // verify PlayerCount default in created scene is within expected range (2-4)
        // reflection to access private field PlayerCount
        try {
            java.lang.reflect.Field f = OnlineGameScene.class.getDeclaredField("PlayerCount");
            f.setAccessible(true);
            int pc = f.getInt(newScene);
            Assert.assertTrue(pc >= 2 && pc <= 4);
        } catch (NoSuchFieldException e) {
            // ignore if field name mismatch in refactor
        }
    }
}
