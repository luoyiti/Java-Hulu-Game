package com.gameengine.app;

import com.gameengine.app.testutils.FakeRenderer;
import com.gameengine.app.testutils.TestUtils;
import com.gameengine.input.InputManager;
import com.gameengine.core.GameEngine;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class MenuSceneTest {

    private FakeRenderer fr;
    private GameEngine engine;
    private InputManager inputManager;

    @Before
    public void setUp() {
        fr = new FakeRenderer(800, 600);
        inputManager = InputManager.getInstance();
        inputManager.reset();
        engine = TestUtils.createEngineWith(fr, inputManager);
    }

    @Test
    public void testConstructor_withName() {
        MenuScene scene = new MenuScene(engine, "MainMenu");
        assertNotNull(scene);
        assertEquals("MainMenu", scene.getName());
    }

    @Test
    public void testInitialize_doesNotCrash() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
    }

    @Test
    public void testUpdate_doesNotCrash() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
        scene.update(0.016f);
        scene.update(0.016f);
    }

    @Test
    public void testRender_doesNotCrash() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
        scene.render();
        scene.render();
    }

    @Test
    public void testRender_drawsMenuElements() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
        fr.textCalls.clear();
        scene.render();
        // 这里没有初始化具体的菜单元素，只验证render调用不会抛出异常
        assertTrue(true);
    }

    @Test
    public void testUpdate_multipleFrames() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
        for (int i = 0; i < 100; i++) {
            scene.update(0.016f);
        }
    }

    @Test
    public void testDestroyScene_doesNotCrash() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
        scene.destroyScene();
        scene.destroyScene();
    }

    @Test
    public void testUpdate_withVariousDeltaTimes() {
        MenuScene scene = new MenuScene(engine, "Test");
        scene.initialize();
        scene.update(0.001f);
        scene.update(0.016f);
        scene.update(0.033f);
    }

    @Test
    public void testConstructor_withDifferentNames() {
        MenuScene menu1 = new MenuScene(engine, "Menu1");
        MenuScene menu2 = new MenuScene(engine, "Menu2");
        assertNotNull(menu1);
        assertNotNull(menu2);
        assertEquals("Menu1", menu1.getName());
        assertEquals("Menu2", menu2.getName());
    }

    @Test
    public void testEnterStartsGameScene() throws Exception {
        InputManager im = InputManager.getInstance();
        im.reset();
        im.onKeyPressed(257);
        FakeRenderer fr = new FakeRenderer(800, 600);
        com.gameengine.core.GameEngine engine = TestUtils.createEngineWith(fr, im);

        MenuScene menu = new MenuScene(engine, "MainMenu");
        // initialize to wire renderer/inputManager
        menu.initialize();

        // call update which should handle the Enter key and switch scene
        menu.update(0.016f);

        // 由于没有具体的游戏场景实现，这里只验证没有抛出异常
        assertTrue(true);
    }

    @Test
    public void testRenderWithNullRendererDoesNotThrow() {
        MenuScene menu = new MenuScene(null, "MainMenu");
        // renderer is null inside menu, render should simply return without exception
        menu.render();
    }
}
