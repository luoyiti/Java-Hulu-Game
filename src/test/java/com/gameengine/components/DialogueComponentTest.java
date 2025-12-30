package com.gameengine.components;

import com.gameengine.core.GameObject;
import com.gameengine.dialogue.DialogueManager;
import com.gameengine.dialogue.DialogueNode;
import com.gameengine.dialogue.DialogueTriggerType;
import com.gameengine.math.Vector2;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DialogueComponentTest {

    @Before
    public void setUp() {
        DialogueManager.getInstance().reset();
    }

    @Test
    public void testManualTriggerShowsHintAndInteractStartsDialogue() {
        DialogueNode node = new DialogueNode("NPC", "Hello");
        DialogueComponent component = new DialogueComponent(node, DialogueTriggerType.MANUAL);

        GameObject npc = new GameObject("npc");
        npc.addComponent(new TransformComponent(new Vector2(0, 0)));
        npc.addComponent(component);

        GameObject player = new GameObject("player");
        player.setPlayer();
        player.addComponent(new TransformComponent(new Vector2(10, 0)));

        component.setPlayer(player);
        component.update(0.016f);

        assertTrue("接近时应显示交互提示", component.isShowInteractionHint());
        component.interact();

        assertTrue(DialogueManager.getInstance().isDialogueActive());
        assertEquals(node, DialogueManager.getInstance().getCurrentNode());
        assertTrue(component.hasTriggered());
        assertFalse(component.isShowInteractionHint());
    }

    @Test
    public void testDistanceTriggerAutoStartsAndNonRepeatable() {
        DialogueNode node = new DialogueNode("NPC", "Auto");
        DialogueComponent component = new DialogueComponent(node, DialogueTriggerType.DISTANCE);

        GameObject npc = new GameObject("npc");
        npc.addComponent(new TransformComponent(new Vector2(0, 0)));
        npc.addComponent(component);

        GameObject player = new GameObject("player");
        player.setPlayer();
        player.addComponent(new TransformComponent(new Vector2(5, 0)));

        component.setPlayer(player);
        component.update(0.016f);

        assertTrue(DialogueManager.getInstance().isDialogueActive());
        assertTrue(component.hasTriggered());

        DialogueManager.getInstance().skipDialogue();
        assertFalse(DialogueManager.getInstance().isDialogueActive());

        component.update(0.016f);
        assertFalse("不可重复触发时不应再次激活对话", DialogueManager.getInstance().isDialogueActive());
    }

    @Test
    public void testEventTriggerRegistersDialogue() {
        DialogueNode node = new DialogueNode("Narrator", "Start");
        DialogueComponent component = new DialogueComponent(node, DialogueTriggerType.GAME_START);

        GameObject npc = new GameObject("npc");
        npc.addComponent(new TransformComponent(new Vector2(0, 0)));
        npc.addComponent(component);

        DialogueManager.getInstance().triggerEvent(DialogueTriggerType.GAME_START);
        assertTrue(DialogueManager.getInstance().isDialogueActive());
        assertEquals(node, DialogueManager.getInstance().getCurrentNode());
    }
}

