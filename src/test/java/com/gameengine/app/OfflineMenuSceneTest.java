package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.input.InputManager;
import org.junit.Assert;
import org.junit.Test;

public class OfflineMenuSceneTest {

    @Test
    public void testSelectPlayerCountWithKeys() throws Exception {
        InputManager im = InputManager.getInstance();
        im.reset();

        FakeRenderer fr = new FakeRenderer(800, 600);
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith(fr, im);

        OfflineMenuScene scene = new OfflineMenuScene("Offline Menu", engine);
        scene.initialize();

        // simulate right arrow then enter
        im.onKeyPressed(262); // right
        im.onKeyPressed(257); // enter

        scene.update(0.016f);

        Assert.assertNotNull(engine.getCurrentScene());
        Assert.assertTrue(engine.getCurrentScene() instanceof OfflineGameScene);
        // selected player count should be updated on the menu scene
        Assert.assertTrue(scene.getSelectedPlayerCount() >= 2 && scene.getSelectedPlayerCount() <= 4);
    }
}
