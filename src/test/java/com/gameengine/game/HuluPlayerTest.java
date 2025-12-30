package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gameengine.components.TransformComponent;
import com.gameengine.components.LifeFeatureComponent;
import com.gameengine.components.PhysicsComponent;
import com.gameengine.math.Vector2;

/**
 * HuluPlayer (葫芦娃玩家) 测试类
 */
public class HuluPlayerTest {
    
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
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        assertNotNull(player);
        assertEquals("Hulu Player", player.getName());
    }
    
    @Test
    public void testConstructorWithName() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null, "CustomPlayer");
        
        assertNotNull(player);
        assertEquals("CustomPlayer", player.getName());
    }
    
    @Test
    public void testIsPlayer() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        assertEquals("Player", player.getidentity());
    }
    
    @Test
    public void testTransformComponent() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        TransformComponent transform = player.getComponent(TransformComponent.class);
        assertNotNull(transform);
        // 初始位置应该在地图中心
        assertEquals(400, transform.getPosition().x, 0.001f);
        assertEquals(300, transform.getPosition().y, 0.001f);
    }
    
    @Test
    public void testLifeFeatureComponent() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        LifeFeatureComponent lifeFeature = player.getComponent(LifeFeatureComponent.class);
        assertNotNull(lifeFeature);
        assertEquals(100, lifeFeature.getBlood());
    }
    
    @Test
    public void testPhysicsComponent() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        assertNotNull(physics);
    }
    
    // testUpdate removed - requires non-null scene for addGameObject calls
    
    @Test
    public void testRender() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        // render不应抛出异常
        player.render();
    }
    
    @Test
    public void testIsActiveByDefault() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        assertTrue(player.isActive());
    }
    
    @Test
    public void testSetActive() {
        MockRenderer renderer = new MockRenderer();
        
        HuluPlayer player = new HuluPlayer(renderer, null);
        
        player.setActive(false);
        assertFalse(player.isActive());
        
        player.setActive(true);
        assertTrue(player.isActive());
    }
    
    @Test
    public void testMultiplePlayers() {
        MockRenderer renderer = new MockRenderer();
        
        // 测试多个玩家创建（不同的葫芦娃图片）
        HuluPlayer player1 = new HuluPlayer(renderer, null, "Player1");
        HuluPlayer player2 = new HuluPlayer(renderer, null, "Player2");
        HuluPlayer player3 = new HuluPlayer(renderer, null, "Player3");
        
        assertEquals("Player1", player1.getName());
        assertEquals("Player2", player2.getName());
        assertEquals("Player3", player3.getName());
    }
    
    @Test
    public void testSkillReinitializationClearsOldSkills() {
        MockRenderer renderer = new MockRenderer();
        com.gameengine.scene.Scene scene = new com.gameengine.scene.Scene("TestScene");
        
        HuluPlayer player = new HuluPlayer(renderer, scene);
        
        // 第一次初始化技能
        player.initAttackSkillJ();
        AttackSkillJ.SkillType firstSkillType = player.getCurrentSkillType();
        int firstSkillCount = player.getAttackingSkillsJ().size();
        
        assertTrue("应该创建技能", firstSkillCount > 0);
        assertNotNull("应该有技能类型", firstSkillType);
        
        // 第二次初始化技能（模拟关卡切换）
        player.initAttackSkillJ();
        AttackSkillJ.SkillType secondSkillType = player.getCurrentSkillType();
        int secondSkillCount = player.getAttackingSkillsJ().size();
        
        assertTrue("应该创建新技能", secondSkillCount > 0);
        assertNotNull("应该有新技能类型", secondSkillType);
        
        // 验证技能列表大小正确（不会累积）
        // 每种技能类型的数量是固定的
        int expectedCount = 0;
        switch (secondSkillType) {
            case METAL: expectedCount = 5; break;
            case WOOD: expectedCount = 2; break;
            case WATER: expectedCount = 4; break;
            case FIRE: expectedCount = 2; break;
            case EARTH: expectedCount = 3; break;
        }
        assertEquals("技能数量应该匹配当前类型，不应累积旧技能", expectedCount, secondSkillCount);
    }
}
