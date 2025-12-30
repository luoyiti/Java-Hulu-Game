package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.core.GameObject;
import com.gameengine.dialogue.DialogueManager;
import com.gameengine.dialogue.DialogueNode;
import com.gameengine.dialogue.DialogueTriggerType;
import com.gameengine.math.Vector2;

/**
 * 对话组件
 * 可附加到任何 GameObject 上，管理该对象的对话内容和触发逻辑
 */
public class DialogueComponent extends Component<DialogueComponent> {
    
    // 对话内容
    private DialogueNode dialogueTree;
    
    // 触发设置
    private DialogueTriggerType triggerType;
    private float triggerDistance = 100f;  // 距离触发的范围
    private boolean hasTriggered = false;  // 是否已触发过（用于一次性对话）
    private boolean repeatable = false;    // 是否可重复触发
    
    // 交互提示
    private boolean showInteractionHint = false;
    private String interactionHint = "按 E 对话";
    
    // 玩家引用（用于距离检测）
    private GameObject player;
    
    public DialogueComponent() {
        super();
        this.triggerType = DialogueTriggerType.MANUAL;
    }
    
    public DialogueComponent(DialogueNode dialogueTree) {
        this();
        this.dialogueTree = dialogueTree;
    }
    
    public DialogueComponent(DialogueNode dialogueTree, DialogueTriggerType triggerType) {
        this(dialogueTree);
        this.triggerType = triggerType;
    }
    
    @Override
    public void initialize() {
        // 如果是事件触发类型，注册到 DialogueManager
        if (triggerType == DialogueTriggerType.GAME_START ||
            triggerType == DialogueTriggerType.GAME_OVER ||
            triggerType == DialogueTriggerType.VICTORY ||
            triggerType == DialogueTriggerType.DEFEAT ||
            triggerType == DialogueTriggerType.LEVEL_COMPLETE ||
            triggerType == DialogueTriggerType.BOSS_ENCOUNTER) {
            
            DialogueManager.getInstance().registerDialogue(triggerType, dialogueTree);
        }
    }
    
    @Override
    public void update(float deltaTime) {
        if (!enabled || dialogueTree == null) return;
        
        // 如果对话正在进行，不处理新的触发
        if (DialogueManager.getInstance().isDialogueActive()) {
            showInteractionHint = false;
            return;
        }
        
        // 已触发且不可重复
        if (hasTriggered && !repeatable) {
            showInteractionHint = false;
            return;
        }
        
        // 距离触发检测
        if (triggerType == DialogueTriggerType.DISTANCE || triggerType == DialogueTriggerType.MANUAL) {
            checkDistanceTrigger();
        }
    }
    
    /**
     * 检测玩家距离并处理触发
     */
    private void checkDistanceTrigger() {
        if (player == null || owner == null) return;
        
        TransformComponent ownerTransform = owner.getComponent(TransformComponent.class);
        TransformComponent playerTransform = player.getComponent(TransformComponent.class);
        
        if (ownerTransform == null || playerTransform == null) return;
        
        Vector2 ownerPos = ownerTransform.getPosition();
        Vector2 playerPos = playerTransform.getPosition();
        
        float distance = ownerPos.subtract(playerPos).magnitude();
        
        if (distance <= triggerDistance) {
            if (triggerType == DialogueTriggerType.DISTANCE) {
                // 距离触发：自动开始对话
                triggerDialogue();
            } else if (triggerType == DialogueTriggerType.MANUAL) {
                // 手动触发：显示交互提示
                showInteractionHint = true;
            }
        } else {
            showInteractionHint = false;
        }
    }
    
    /**
     * 触发对话
     */
    public void triggerDialogue() {
        if (dialogueTree == null) return;
        if (hasTriggered && !repeatable) return;
        
        DialogueManager.getInstance().startDialogue(dialogueTree);
        hasTriggered = true;
        showInteractionHint = false;
    }
    
    /**
     * 手动触发对话（响应按键）
     */
    public void interact() {
        if (showInteractionHint && triggerType == DialogueTriggerType.MANUAL) {
            triggerDialogue();
        }
    }
    
    @Override
    public void render() {
        // 交互提示的渲染由 DialogueManager 或场景统一处理
    }
    
    // ========== Getter/Setter 方法 ==========
    
    public DialogueNode getDialogueTree() {
        return dialogueTree;
    }
    
    public void setDialogueTree(DialogueNode dialogueTree) {
        this.dialogueTree = dialogueTree;
    }
    
    public DialogueTriggerType getTriggerType() {
        return triggerType;
    }
    
    public void setTriggerType(DialogueTriggerType triggerType) {
        this.triggerType = triggerType;
    }
    
    public float getTriggerDistance() {
        return triggerDistance;
    }
    
    public void setTriggerDistance(float triggerDistance) {
        this.triggerDistance = triggerDistance;
    }
    
    public boolean isRepeatable() {
        return repeatable;
    }
    
    public void setRepeatable(boolean repeatable) {
        this.repeatable = repeatable;
    }
    
    public boolean isShowInteractionHint() {
        return showInteractionHint;
    }
    
    public String getInteractionHint() {
        return interactionHint;
    }
    
    public void setInteractionHint(String interactionHint) {
        this.interactionHint = interactionHint;
    }
    
    public void setPlayer(GameObject player) {
        this.player = player;
    }
    
    public boolean hasTriggered() {
        return hasTriggered;
    }
    
    public void resetTrigger() {
        this.hasTriggered = false;
    }
}
