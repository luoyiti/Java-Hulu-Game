package com.gameengine.dialogue;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DialogueConfigurator 测试类
 */
public class DialogueConfiguratorTest {
    
    // 模拟渲染器用于测试
    private static class MockRenderer implements com.gameengine.graphics.IRenderer {
        @Override
        public void beginFrame() {}
        @Override
        public void endFrame() {}
        @Override
        public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {}
        @Override
        public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {}
        @Override
        public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {}
        @Override
        public void drawText(String text, float x, float y, float fontSize, float r, float g, float b, float a) {}
        @Override
        public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {}
        @Override
        public void drawImage(String imagePath, float x, float y, float width, float height, float alpha) {}
        @Override
        public void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha) {}
        @Override
        public boolean shouldClose() { return false; }
        @Override
        public void pollEvents() {}
        @Override
        public void cleanup() {}
        @Override
        public int getWidth() { return 800; }
        @Override
        public int getHeight() { return 600; }
        @Override
        public String getTitle() { return "Test"; }
    }
    
    @Test
    public void testConstructor() {
        MockRenderer renderer = new MockRenderer();
        
        DialogueConfigurator configurator = new DialogueConfigurator(renderer);
        
        assertNotNull(configurator);
        assertNotNull(configurator.getDialogueManager());
    }
    
    @Test
    public void testInitializeAllDialogues() {
        MockRenderer renderer = new MockRenderer();
        DialogueConfigurator configurator = new DialogueConfigurator(renderer);
        
        // initializeAllDialogues 不应抛出异常
        configurator.initializeAllDialogues();
    }
    
    @Test
    public void testTriggerGameStart() {
        MockRenderer renderer = new MockRenderer();
        DialogueConfigurator configurator = new DialogueConfigurator(renderer);
        configurator.initializeAllDialogues();
        
        // triggerGameStart 不应抛出异常
        configurator.triggerGameStart();
    }
    
    @Test
    public void testGetDialogueManager() {
        MockRenderer renderer = new MockRenderer();
        DialogueConfigurator configurator = new DialogueConfigurator(renderer);
        
        DialogueManager manager = configurator.getDialogueManager();
        assertNotNull(manager);
        
        // 应该返回单例
        assertSame(DialogueManager.getInstance(), manager);
    }
    
    @Test
    public void testTriggerLevelComplete() {
        MockRenderer renderer = new MockRenderer();
        DialogueConfigurator configurator = new DialogueConfigurator(renderer);
        configurator.initializeAllDialogues();
        
        // triggerLevelComplete 不应抛出异常
        configurator.triggerLevelComplete(1);
        configurator.triggerLevelComplete(5);
        configurator.triggerLevelComplete(10);
    }
}
