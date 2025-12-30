package com.gameengine.dialogue;

import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;

import java.util.*;

/**
 * 对话管理器
 * 单例模式，管理全局对话状态、队列和事件触发
 */
public class DialogueManager {
    private static DialogueManager instance;
    
    // 对话状态
    private boolean dialogueActive;
    private DialogueNode currentNode;
    private Queue<DialogueNode> dialogueQueue;
    
    // 打字机效果
    private String displayedText;
    private int charIndex;
    private float charTimer;
    private float charDelay = 0.03f; // 每个字符显示间隔
    private boolean textComplete;
    
    // 事件监听器
    private Map<DialogueTriggerType, List<DialogueNode>> eventDialogues;
    
    // 输入冷却
    private float inputCooldown = 0f;
    private static final float INPUT_COOLDOWN_TIME = 0.2f;
    
    // 渲染器引用
    private DialogueRenderer dialogueRenderer;
    
    private DialogueManager() {
        this.dialogueActive = false;
        this.currentNode = null;
        this.dialogueQueue = new LinkedList<>();
        this.displayedText = "";
        this.charIndex = 0;
        this.charTimer = 0f;
        this.textComplete = false;
        this.eventDialogues = new HashMap<>();
        
        // 初始化所有事件类型的列表
        for (DialogueTriggerType type : DialogueTriggerType.values()) {
            eventDialogues.put(type, new ArrayList<>());
        }
    }
    
    public static DialogueManager getInstance() {
        if (instance == null) {
            instance = new DialogueManager();
        }
        return instance;
    }
    
    /**
     * 重置对话管理器（用于场景切换）
     */
    public void reset() {
        this.dialogueActive = false;
        this.currentNode = null;
        this.dialogueQueue.clear();
        this.displayedText = "";
        this.charIndex = 0;
        this.textComplete = false;
        
        // 清空事件对话
        for (DialogueTriggerType type : DialogueTriggerType.values()) {
            eventDialogues.put(type, new ArrayList<>());
        }
    }
    
    /**
     * 初始化渲染器
     */
    public void initRenderer(IRenderer renderer) {
        this.dialogueRenderer = new DialogueRenderer(renderer);
    }
    
    // ========== 对话注册 ==========
    
    /**
     * 注册事件触发的对话
     */
    public void registerDialogue(DialogueTriggerType triggerType, DialogueNode dialogue) {
        eventDialogues.get(triggerType).add(dialogue);
    }
    
    /**
     * 触发特定事件的所有对话
     */
    public void triggerEvent(DialogueTriggerType eventType) {
        List<DialogueNode> dialogues = eventDialogues.get(eventType);
        if (dialogues != null && !dialogues.isEmpty()) {
            for (DialogueNode dialogue : dialogues) {
                queueDialogue(dialogue);
            }
        }
    }
    
    /**
     * 将对话加入队列
     */
    public void queueDialogue(DialogueNode dialogue) {
        if (dialogue != null) {
            dialogueQueue.add(dialogue);
            if (!dialogueActive) {
                startNextDialogue();
            }
        }
    }
    
    /**
     * 立即开始对话（跳过队列）
     */
    public void startDialogue(DialogueNode dialogue) {
        if (dialogue != null) {
            currentNode = dialogue;
            dialogueActive = true;
            resetTextDisplay();
        }
    }
    
    // ========== 对话控制 ==========
    
    private void startNextDialogue() {
        if (!dialogueQueue.isEmpty()) {
            currentNode = dialogueQueue.poll();
            dialogueActive = true;
            resetTextDisplay();
        } else {
            dialogueActive = false;
            currentNode = null;
        }
    }
    
    private void resetTextDisplay() {
        displayedText = "";
        charIndex = 0;
        charTimer = 0f;
        textComplete = false;
    }
    
    /**
     * 推进对话（下一句或选择选项）
     */
    public void advanceDialogue() {
        if (!dialogueActive || currentNode == null) return;
        
        // 如果文字还没显示完，直接显示全部
        if (!textComplete) {
            displayedText = currentNode.getText();
            charIndex = displayedText.length();
            textComplete = true;
            return;
        }
        
        // 如果有选项，需要玩家选择
        if (currentNode.hasOptions()) {
            // 选项处理由 selectOption 方法完成
            return;
        }
        
        // 进入下一个节点
        if (currentNode.getNextNode() != null) {
            currentNode = currentNode.getNextNode();
            resetTextDisplay();
        } else {
            // 对话结束，检查队列
            startNextDialogue();
        }
    }
    
    /**
     * 选择对话选项
     */
    public void selectOption(int optionIndex) {
        if (!dialogueActive || currentNode == null) return;
        if (!currentNode.hasOptions()) return;
        
        List<DialogueNode.DialogueOption> options = currentNode.getOptions();
        if (optionIndex >= 0 && optionIndex < options.size()) {
            DialogueNode nextNode = options.get(optionIndex).getTargetNode();
            if (nextNode != null) {
                currentNode = nextNode;
                resetTextDisplay();
            } else {
                startNextDialogue();
            }
        }
    }
    
    /**
     * 跳过当前对话
     */
    public void skipDialogue() {
        dialogueActive = false;
        currentNode = null;
        dialogueQueue.clear();
    }
    
    // ========== 更新和渲染 ==========
    
    /**
     * 更新对话状态（处理打字机效果和输入）
     */
    public void update(float deltaTime, InputManager inputManager) {
        if (!dialogueActive || currentNode == null) return;
        
        // 更新输入冷却
        if (inputCooldown > 0) {
            inputCooldown -= deltaTime;
        }
        
        // 打字机效果
        if (!textComplete && currentNode != null) {
            charTimer += deltaTime;
            while (charTimer >= charDelay && charIndex < currentNode.getText().length()) {
                charIndex++;
                displayedText = currentNode.getText().substring(0, charIndex);
                charTimer -= charDelay;
            }
            if (charIndex >= currentNode.getText().length()) {
                textComplete = true;
            }
        }
        
        // 处理输入
        if (inputCooldown <= 0) {
            // 空格键或回车键推进对话
            if (inputManager.isKeyJustPressed(32) || inputManager.isKeyJustPressed(257)) { // Space or Enter
                advanceDialogue();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }
            
            // ESC键跳过对话
            if (inputManager.isKeyJustPressed(256)) { // ESC
                skipDialogue();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }
            
            // 数字键选择选项 (1-9) - 需要再次检查 currentNode
            if (currentNode != null && currentNode.hasOptions() && textComplete) {
                for (int i = 0; i < Math.min(9, currentNode.getOptions().size()); i++) {
                    if (inputManager.isKeyJustPressed(49 + i)) { // Keys 1-9
                        selectOption(i);
                        inputCooldown = INPUT_COOLDOWN_TIME;
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * 渲染对话框
     */
    public void render() {
        if (!dialogueActive || currentNode == null || dialogueRenderer == null) return;
        
        dialogueRenderer.render(currentNode, displayedText, textComplete);
    }
    
    // ========== Getter 方法 ==========
    
    public boolean isDialogueActive() {
        return dialogueActive;
    }
    
    public DialogueNode getCurrentNode() {
        return currentNode;
    }
    
    public String getDisplayedText() {
        return displayedText;
    }
    
    public boolean isTextComplete() {
        return textComplete;
    }
    
    public void setCharDelay(float delay) {
        this.charDelay = delay;
    }
}
