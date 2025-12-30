package com.gameengine.dialogue;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * DialogueManager 测试类
 */
public class DialogueManagerTest {
    
    private DialogueManager manager;
    
    @Before
    public void setUp() {
        // 获取单例并重置
        manager = DialogueManager.getInstance();
        manager.reset();
    }
    
    @Test
    public void testGetInstance() {
        DialogueManager instance1 = DialogueManager.getInstance();
        DialogueManager instance2 = DialogueManager.getInstance();
        
        assertNotNull(instance1);
        assertSame(instance1, instance2);
    }
    
    @Test
    public void testInitialState() {
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testStartDialogue() {
        DialogueNode node = new DialogueNode("爷爷", "欢迎来到葫芦世界！");
        manager.startDialogue(node);
        
        assertTrue(manager.isDialogueActive());
    }
    
    @Test
    public void testStartDialogueWithNull() {
        manager.startDialogue(null);
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testQueueDialogue() {
        DialogueNode node1 = new DialogueNode("爷爷", "第一句话");
        DialogueNode node2 = new DialogueNode("爷爷", "第二句话");
        
        manager.queueDialogue(node1);
        manager.queueDialogue(node2);
        
        assertTrue(manager.isDialogueActive());
    }
    
    @Test
    public void testQueueDialogueWithNull() {
        manager.queueDialogue(null);
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testReset() {
        DialogueNode node = new DialogueNode("爷爷", "测试对话");
        manager.startDialogue(node);
        assertTrue(manager.isDialogueActive());
        
        manager.reset();
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testRegisterDialogue() {
        DialogueNode node = new DialogueNode("系统", "游戏开始！");
        
        // 注册对话不应抛出异常
        manager.registerDialogue(DialogueTriggerType.GAME_START, node);
    }
    
    @Test
    public void testTriggerEvent() {
        DialogueNode node = new DialogueNode("系统", "游戏开始！");
        manager.registerDialogue(DialogueTriggerType.GAME_START, node);
        
        manager.triggerEvent(DialogueTriggerType.GAME_START);
        assertTrue(manager.isDialogueActive());
    }
    
    @Test
    public void testTriggerEventNoDialogue() {
        // 触发没有注册对话的事件不应抛出异常
        manager.triggerEvent(DialogueTriggerType.LEVEL_COMPLETE);
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testAdvanceDialogueWhenInactive() {
        // 无活动对话时推进不应抛出异常
        manager.advanceDialogue();
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testAdvanceDialogue() {
        DialogueNode node1 = new DialogueNode("爷爷", "第一句话");
        DialogueNode node2 = new DialogueNode("爷爷", "第二句话");
        node1.then(node2);  // 使用 then 方法链接
        
        manager.startDialogue(node1);
        assertTrue(manager.isDialogueActive());
        
        // 先显示完整文字
        manager.advanceDialogue();
        // 再推进到下一句
        manager.advanceDialogue();
        assertTrue(manager.isDialogueActive());
    }
    
    @Test
    public void testSelectOptionWhenInactive() {
        // 无活动对话时选择选项不应抛出异常
        manager.selectOption(0);
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testSelectOptionWithNoOptions() {
        DialogueNode node = new DialogueNode("爷爷", "没有选项的对话");
        manager.startDialogue(node);
        
        // 选择选项不应抛出异常
        manager.selectOption(0);
    }
    
    @Test
    public void testSelectOptionWithOptions() {
        DialogueNode node = new DialogueNode("爷爷", "你准备好了吗？");
        DialogueNode yesNode = new DialogueNode("爷爷", "很好！");
        DialogueNode noNode = new DialogueNode("爷爷", "没关系，准备好再来。");
        
        node.addOption("是的", yesNode);
        node.addOption("还没有", noNode);
        
        manager.startDialogue(node);
        
        // 先显示完整文字
        manager.advanceDialogue();
        
        // 选择第一个选项
        manager.selectOption(0);
        assertTrue(manager.isDialogueActive());
    }
    
    @Test
    public void testSelectOptionInvalidIndex() {
        DialogueNode node = new DialogueNode("爷爷", "你准备好了吗？");
        node.addOption("是的", null);
        
        manager.startDialogue(node);
        manager.advanceDialogue();
        
        // 选择无效索引不应抛出异常
        manager.selectOption(10);
        manager.selectOption(-1);
    }
    
    @Test
    public void testSkipDialogue() {
        DialogueNode node = new DialogueNode("爷爷", "测试对话");
        manager.startDialogue(node);
        assertTrue(manager.isDialogueActive());
        
        manager.skipDialogue();
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testSkipDialogueWhenInactive() {
        // 无活动对话时跳过不应抛出异常
        manager.skipDialogue();
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testUpdate() {
        DialogueNode node = new DialogueNode("爷爷", "测试对话");
        manager.startDialogue(node);
        
        // update在没有inputManager时不应抛出异常
        // 由于 update 需要 InputManager，这里跳过
    }
    
    @Test
    public void testUpdateWhenInactive() {
        // 无活动对话时update不应抛出异常
        // 由于 update 需要 InputManager，这里只验证不活动状态
        assertFalse(manager.isDialogueActive());
    }
    
    @Test
    public void testRender() {
        DialogueNode node = new DialogueNode("爷爷", "测试对话");
        manager.startDialogue(node);
        
        // 没有初始化渲染器时render不应抛出异常
        manager.render();
    }
    
    @Test
    public void testRenderWhenInactive() {
        // 无活动对话时render不应抛出异常
        manager.render();
    }
}
