package com.gameengine.dialogue;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DialogueRenderer 测试类
 */
public class DialogueRendererTest {
    
    // 模拟渲染器用于测试
    private static class MockRenderer implements com.gameengine.graphics.IRenderer {
        public int drawRectCount = 0;
        public int drawTextCount = 0;
        
        @Override
        public void beginFrame() {}
        @Override
        public void endFrame() {}
        @Override
        public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {
            drawRectCount++;
        }
        @Override
        public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {}
        @Override
        public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {}
        @Override
        public void drawText(String text, float x, float y, float fontSize, float r, float g, float b, float a) {
            drawTextCount++;
        }
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
        
        DialogueRenderer dialogueRenderer = new DialogueRenderer(renderer);
        
        assertNotNull(dialogueRenderer);
    }
    
    @Test
    public void testRenderWithNull() {
        MockRenderer renderer = new MockRenderer();
        DialogueRenderer dialogueRenderer = new DialogueRenderer(renderer);
        
        // 传入null不应抛出异常
        dialogueRenderer.render(null, "", false);
    }
    
    @Test
    public void testRenderWithDialogue() {
        MockRenderer renderer = new MockRenderer();
        DialogueRenderer dialogueRenderer = new DialogueRenderer(renderer);
        
        DialogueNode node = new DialogueNode("爷爷", "欢迎来到葫芦世界！");
        
        // 渲染不应抛出异常
        dialogueRenderer.render(node, "欢迎来到葫芦世界！", true);
        
        // 验证调用了渲染方法
        assertTrue(renderer.drawRectCount > 0);
    }
    
    @Test
    public void testRenderWithOptions() {
        MockRenderer renderer = new MockRenderer();
        DialogueRenderer dialogueRenderer = new DialogueRenderer(renderer);
        
        DialogueNode node = new DialogueNode("爷爷", "你准备好了吗？");
        node.addOption("是的", null);
        node.addOption("还没有", null);
        
        // 渲染带选项的对话不应抛出异常
        dialogueRenderer.render(node, "你准备好了吗？", true);
    }
    
    @Test
    public void testRenderIncompleteText() {
        MockRenderer renderer = new MockRenderer();
        DialogueRenderer dialogueRenderer = new DialogueRenderer(renderer);
        
        DialogueNode node = new DialogueNode("爷爷", "这是一段很长的对话文本...");
        
        // 文本未完成时渲染
        dialogueRenderer.render(node, "这是一段", false);
    }
    
    @Test
    public void testRenderDifferentSpeakerTypes() {
        MockRenderer renderer = new MockRenderer();
        DialogueRenderer dialogueRenderer = new DialogueRenderer(renderer);
        
        // 玩家类型
        DialogueNode playerNode = new DialogueNode("大娃", "我来啦！", 
            DialogueNode.SpeakerType.PLAYER, "resources/picture/huluBro1.png");
        dialogueRenderer.render(playerNode, "我来啦！", true);
        
        // 敌人类型
        DialogueNode enemyNode = new DialogueNode("蛇精", "休想逃走！", 
            DialogueNode.SpeakerType.ENEMY, "resources/picture/snake_queen.png");
        dialogueRenderer.render(enemyNode, "休想逃走！", true);
        
        // 旁白类型
        DialogueNode narratorNode = new DialogueNode("旁白", "战斗开始了...");
        narratorNode.withType(DialogueNode.SpeakerType.NARRATOR);
        dialogueRenderer.render(narratorNode, "战斗开始了...", true);
    }
}
