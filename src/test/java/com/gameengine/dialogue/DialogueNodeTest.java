package com.gameengine.dialogue;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.gameengine.dialogue.DialogueNode.SpeakerType;
import com.gameengine.dialogue.DialogueNode.DialogueOption;

/**
 * DialogueNode 测试类
 */
public class DialogueNodeTest {
    
    private DialogueNode node;
    
    @Before
    public void setUp() {
        node = new DialogueNode("TestSpeaker", "Test text");
    }
    
    @Test
    public void testBasicConstructor() {
        assertEquals("TestSpeaker", node.getSpeaker());
        assertEquals("Test text", node.getText());
        assertEquals(SpeakerType.NARRATOR, node.getSpeakerType());
        assertNull(node.getPortraitPath());
        assertNull(node.getNextNode());
        assertFalse(node.hasOptions());
    }
    
    @Test
    public void testConstructorWithPortrait() {
        DialogueNode nodeWithPortrait = new DialogueNode("Speaker", "Text", "path/to/portrait.png");
        assertEquals("Speaker", nodeWithPortrait.getSpeaker());
        assertEquals("Text", nodeWithPortrait.getText());
        assertEquals("path/to/portrait.png", nodeWithPortrait.getPortraitPath());
    }
    
    @Test
    public void testConstructorWithTypeAndPortrait() {
        DialogueNode nodeWithType = new DialogueNode("Player", "Hello!", SpeakerType.PLAYER, "player.png");
        assertEquals(SpeakerType.PLAYER, nodeWithType.getSpeakerType());
        assertEquals("player.png", nodeWithType.getPortraitPath());
    }
    
    @Test
    public void testThenMethod() {
        DialogueNode secondNode = new DialogueNode("Speaker2", "Second text");
        DialogueNode result = node.then(secondNode);
        
        assertEquals(secondNode, node.getNextNode());
        assertEquals(secondNode, result);
        assertTrue(node.hasNext());
    }
    
    @Test
    public void testChainedThen() {
        DialogueNode node1 = new DialogueNode("S1", "Text1");
        DialogueNode node2 = new DialogueNode("S2", "Text2");
        DialogueNode node3 = new DialogueNode("S3", "Text3");
        
        node1.then(node2).then(node3);
        
        assertEquals(node2, node1.getNextNode());
        assertEquals(node3, node2.getNextNode());
    }
    
    @Test
    public void testAddOption() {
        DialogueNode targetNode = new DialogueNode("Target", "Target text");
        node.addOption("Option 1", targetNode);
        
        assertTrue(node.hasOptions());
        assertEquals(1, node.getOptions().size());
        
        DialogueOption option = node.getOptions().get(0);
        assertEquals("Option 1", option.getText());
        assertEquals(targetNode, option.getTargetNode());
    }
    
    @Test
    public void testMultipleOptions() {
        DialogueNode target1 = new DialogueNode("T1", "Text1");
        DialogueNode target2 = new DialogueNode("T2", "Text2");
        
        node.addOption("Go left", target1).addOption("Go right", target2);
        
        assertEquals(2, node.getOptions().size());
        assertTrue(node.hasNext());
    }
    
    @Test
    public void testWithType() {
        DialogueNode result = node.withType(SpeakerType.ENEMY);
        assertEquals(SpeakerType.ENEMY, node.getSpeakerType());
        assertEquals(node, result);
    }
    
    @Test
    public void testWithPortrait() {
        DialogueNode result = node.withPortrait("new/portrait.png");
        assertEquals("new/portrait.png", node.getPortraitPath());
        assertEquals(node, result);
    }
    
    @Test
    public void testHasNextWithNoNextAndNoOptions() {
        assertFalse(node.hasNext());
    }
    
    @Test
    public void testSpeakerTypeValues() {
        assertEquals(3, SpeakerType.values().length);
        assertEquals(SpeakerType.PLAYER, SpeakerType.valueOf("PLAYER"));
        assertEquals(SpeakerType.ENEMY, SpeakerType.valueOf("ENEMY"));
        assertEquals(SpeakerType.NARRATOR, SpeakerType.valueOf("NARRATOR"));
    }
    
    @Test
    public void testCreateChainWithSingleText() {
        DialogueNode chain = DialogueNode.createChain("Speaker", "Single text");
        assertNotNull(chain);
        assertEquals("Single text", chain.getText());
    }
    
    @Test
    public void testCreateChainWithMultipleTexts() {
        DialogueNode chain = DialogueNode.createChain("Speaker", "Text1", "Text2", "Text3");
        assertNotNull(chain);
        assertEquals("Text1", chain.getText());
        assertEquals("Text2", chain.getNextNode().getText());
        assertEquals("Text3", chain.getNextNode().getNextNode().getText());
    }
    
    @Test
    public void testCreateChainWithEmptyTexts() {
        DialogueNode chain = DialogueNode.createChain("Speaker");
        assertNull(chain);
    }
}
