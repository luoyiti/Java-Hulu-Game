package com.gameengine.core;

import com.gameengine.components.PhysicsComponent;
import com.gameengine.components.TransformComponent;
import com.gameengine.game.HuluPlayer;
import com.gameengine.game.Record;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;
import com.gameengine.scene.Scene;
import org.junit.Test;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class GameLogicTest {

    private static final class MockRenderer implements IRenderer {
        @Override public void beginFrame() {}
        @Override public void endFrame() {}
        @Override public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {}
        @Override public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {}
        @Override public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {}
        @Override public void drawText(String text, float x, float y, float size, float r, float g, float b, float a) {}
        @Override public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {}
        @Override public void drawImage(String imagePath, float x, float y, float width, float height, float alpha) {}
        @Override public void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha) {}
        @Override public boolean shouldClose() { return false; }
        @Override public void pollEvents() {}
        @Override public void cleanup() {}
        @Override public int getWidth() { return 800; }
        @Override public int getHeight() { return 600; }
        @Override public String getTitle() { return "test"; }
    }

    @Test
    public void testGetPlayerAndCollections() {
        Scene scene = new Scene("test");

        GameObject player = new GameObject("P1");
        player.setPlayer();
        player.addComponent(new TransformComponent(new Vector2(0, 0)));
        player.addComponent(new PhysicsComponent());

        GameObject enemy = new GameObject("E1");
        enemy.setEnemy();
        enemy.addComponent(new TransformComponent(new Vector2(10, 10)));
        enemy.addComponent(new PhysicsComponent());

        GameObject skill = new GameObject("S1");
        skill.setPlayerSkill();
        skill.addComponent(new TransformComponent(new Vector2(5, 5)));

        scene.addGameObject(player);
        scene.addGameObject(enemy);
        scene.addGameObject(skill);
        scene.update(0f);

        try (GameLogic logic = new GameLogic(scene, null)) {
            assertNotNull("应该能找到玩家对象", logic.getPlayer());
            assertEquals("应该有一个玩家", 1, logic.getPlayers().size());
            assertEquals("应该有一个敌人", 1, logic.getEnemies().size());
            assertEquals("应该有一个技能", 1, logic.getSkills().size());
        }
    }

    @Test
    public void testCooldownAndRecords() throws Exception {
        Scene scene = new Scene("test");
        IRenderer renderer = new MockRenderer();

        HuluPlayer player = new HuluPlayer(renderer, scene);
        scene.addGameObject(player);
        scene.update(0f);

        try (GameLogic logic = new GameLogic(scene, null)) {
            assertEquals(1.0f, logic.getSkillCooldownPercentage(), 0.0001f);

            logic.handlePlayerAttackJ();
            assertEquals(0.0f, logic.getSkillCooldownPercentage(), 0.0001f);

            logic.updateAttack(0.25f);
            assertEquals(0.5f, logic.getSkillCooldownPercentage(), 0.0001f);

            logic.updateAttack(10.0f);
            assertEquals(1.0f, logic.getSkillCooldownPercentage(), 0.0001f);

            Record record = logic.getRecord(123.0f);
            assertEquals(123.0f, record.getKey(), 0.0001f);
            assertEquals("object_move", record.getType());
            assertEquals(100, record.getPlayerHealth());
            assertNotNull(record.getGameObjectsMove());
            assertTrue("应至少记录一个对象", record.getGameObjectsMove().size() >= 1);

            Path targetDir = Paths.get("target");
            Files.createDirectories(targetDir);
            Path out = Files.createTempFile(targetDir, "records-", ".jsonl");
            try (FileWriter writer = new FileWriter(out.toFile())) {
                logic.updateRecords(0.0f, writer);
            }

            String content = Files.readString(out);
            assertTrue(content.contains("object_move"));
            assertTrue(content.contains("skillCooldownPercent"));
        }
    }
}

