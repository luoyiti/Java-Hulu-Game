package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.core.GameEngine;
import com.gameengine.input.InputManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OfflineGameSceneTest {

    private FakeRenderer fr;
    private GameEngine engine;

    @Before
    public void setUp() {
        fr = new FakeRenderer(800, 600);
        InputManager.getInstance().reset();
        engine = TestUtils.createEngineWith(fr, InputManager.getInstance());
    }

    @Test
    public void initialize_createsPlayers_and_cameraSet() {
        OfflineGameScene scene = new OfflineGameScene("Offline", engine, 2);
        scene.initialize();

        // call update to flush objectsToAdd into gameObjects and initialize them
        scene.update(0.016f);

        assertNotNull(scene.getCamera());
        // players array should be non-null and populated
        try {
            java.lang.reflect.Field f = OfflineGameScene.class.getDeclaredField("players");
            f.setAccessible(true);
            Object playersObj = f.get(scene);
            assertNotNull(playersObj);
            Object[] players = (Object[]) playersObj;
            assertEquals(2, players.length);
            assertNotNull("player[0] should be created", players[0]);
        } catch (Exception e) {
            fail("reflection failure: " + e.getMessage());
        }
    }
}
