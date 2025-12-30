package com.gameengine.dialogue;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * DialogueTriggerType 枚举测试类
 */
public class DialogueTriggerTypeTest {
    
    @Test
    public void testAllTriggerTypes() {
        DialogueTriggerType[] types = DialogueTriggerType.values();
        assertTrue(types.length >= 8);
    }
    
    @Test
    public void testGameStart() {
        assertEquals(DialogueTriggerType.GAME_START, DialogueTriggerType.valueOf("GAME_START"));
    }
    
    @Test
    public void testGameOver() {
        assertEquals(DialogueTriggerType.GAME_OVER, DialogueTriggerType.valueOf("GAME_OVER"));
    }
    
    @Test
    public void testVictory() {
        assertEquals(DialogueTriggerType.VICTORY, DialogueTriggerType.valueOf("VICTORY"));
    }
    
    @Test
    public void testDefeat() {
        assertEquals(DialogueTriggerType.DEFEAT, DialogueTriggerType.valueOf("DEFEAT"));
    }
    
    @Test
    public void testLevelComplete() {
        assertEquals(DialogueTriggerType.LEVEL_COMPLETE, DialogueTriggerType.valueOf("LEVEL_COMPLETE"));
    }
    
    @Test
    public void testDistance() {
        assertEquals(DialogueTriggerType.DISTANCE, DialogueTriggerType.valueOf("DISTANCE"));
    }
    
    @Test
    public void testManual() {
        assertEquals(DialogueTriggerType.MANUAL, DialogueTriggerType.valueOf("MANUAL"));
    }
    
    @Test
    public void testBossEncounter() {
        assertEquals(DialogueTriggerType.BOSS_ENCOUNTER, DialogueTriggerType.valueOf("BOSS_ENCOUNTER"));
    }
}
