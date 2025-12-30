package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.core.Camera;
import com.gameengine.input.InputManager;
import com.gameengine.scene.Scene;
import com.gameengine.dialogue.DialogueManager;
import com.gameengine.dialogue.DialogueNode;
import com.gameengine.graphics.IRenderer;
import com.gameengine.level.LevelManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameSceneDiTest {

    private FakeRenderer fakeRenderer;

    @Before
    public void setUp() {
        fakeRenderer = new FakeRenderer();
        InputManager.getInstance().reset();
    }

    @Test
    public void render_withoutCamera_drawsFixedBackground() {
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith((IRenderer) fakeRenderer);
        GameScene scene = new GameScene("test", engine, null, fakeRenderer, null, null, null, null);
        // ensure camera is null
        scene.render();

        assertFalse("No draw calls should be empty", fakeRenderer.drawCalls.isEmpty());
        FakeRenderer.DrawCall last = fakeRenderer.drawCalls.get(fakeRenderer.drawCalls.size() - 1);
        assertEquals(800.0f, last.w, 0.01f);
        assertEquals(600.0f, last.h, 0.01f);
    }

    @Test
    public void render_withCamera_drawsParallaxBackground() {
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith((IRenderer) fakeRenderer);
        GameScene scene = new GameScene("test", engine, null, fakeRenderer, null, null, null, null);
        scene.setCamera(new Camera(800, 600, 2000, 1500));
        scene.render();

        assertFalse(fakeRenderer.drawCalls.isEmpty());
        FakeRenderer.DrawCall last = fakeRenderer.drawCalls.get(fakeRenderer.drawCalls.size() - 1);
        // parallax branch uses large bg size 2000x1500
        assertEquals(2000.0f, last.w, 0.01f);
        assertEquals(1500.0f, last.h, 0.01f);
    }

    @Test
    public void update_escapeKey_switchesToMenuScene() {
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith((IRenderer) fakeRenderer);
        GameScene scene = new GameScene("test", engine, null, fakeRenderer, null, null, null, DialogueManager.getInstance());
        // simulate ESC key just pressed (GLFW code 256)
        InputManager.getInstance().onKeyPressed(256);

        scene.initialize();
        scene.update(0.016f);

        assertNotNull(engine.getCurrentScene());
        assertEquals("MainMenu", engine.getCurrentScene().getName());
    }

    @Test
    public void update_dialogueActive_skipsGameLogicAndDoesNotSpawnEnemies() {
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith((IRenderer) fakeRenderer);

        // named test subclass to avoid anonymous-class warnings
        class TestLevelManager extends LevelManager {
            public boolean called = false;
            public TestLevelManager() {
                super(new com.gameengine.level.EnemyFactory(null, new Scene("s"), null));
            }
            @Override
            public void spawnCurrentLevel() {
                called = true;
            }
            public boolean wasCalled() { return called; }
        }

        TestLevelManager fakeLevelManager = new TestLevelManager();

        DialogueManager dm = DialogueManager.getInstance();
        dm.startDialogue(new DialogueNode("n", "hello"));

        GameScene scene = new GameScene("test", engine, null, fakeRenderer, fakeLevelManager, null, null, dm);
        scene.initialize();
        // enemiesSpawned should still be false and spawnCurrentLevel should NOT be called
        scene.update(0.016f);

        // ensure our fakeLevelManager.spawnCurrentLevel was not invoked while dialogue active
        assertFalse(fakeLevelManager.wasCalled());
    }
}
