package com.gameengine.dialogue;

import java.util.ArrayList;
import java.util.List;

/**
 * 对话节点类
 * 存储单条对话的文本、说话者和选项
 */
public class DialogueNode {
    
    /**
     * 说话者类型枚举
     */
    public enum SpeakerType {
        PLAYER,     // 玩家（头像显示在左边）
        ENEMY,      // 怪物/敌人（头像显示在右边）
        NARRATOR    // 旁白（无头像，居中显示）
    }
    
    private String speaker;        // 说话者名称
    private String text;           // 对话文本
    private List<DialogueOption> options;  // 对话选项（可选）
    private DialogueNode nextNode; // 下一个对话节点（线性对话）
    private String portraitPath;   // 头像图片路径（可选）
    private SpeakerType speakerType; // 说话者类型
    
    public DialogueNode(String speaker, String text) {
        this.speaker = speaker;
        this.text = text;
        this.options = new ArrayList<>();
        this.nextNode = null;
        this.portraitPath = null;
        this.speakerType = SpeakerType.NARRATOR; // 默认为旁白
    }
    
    public DialogueNode(String speaker, String text, String portraitPath) {
        this(speaker, text);
        this.portraitPath = portraitPath;
    }
    
    public DialogueNode(String speaker, String text, SpeakerType type, String portraitPath) {
        this(speaker, text);
        this.speakerType = type;
        this.portraitPath = portraitPath;
    }
    
    // ========== 链式构建方法 ==========
    
    /**
     * 设置下一个对话节点（用于线性对话）
     */
    public DialogueNode then(DialogueNode next) {
        this.nextNode = next;
        return next;
    }
    
    /**
     * 添加对话选项（用于分支对话）
     */
    public DialogueNode addOption(String optionText, DialogueNode targetNode) {
        this.options.add(new DialogueOption(optionText, targetNode));
        return this;
    }
    
    /**
     * 设置说话者类型
     */
    public DialogueNode withType(SpeakerType type) {
        this.speakerType = type;
        return this;
    }
    
    /**
     * 设置头像路径
     */
    public DialogueNode withPortrait(String path) {
        this.portraitPath = path;
        return this;
    }
    
    // ========== Getter 方法 ==========
    
    public String getSpeaker() {
        return speaker;
    }
    
    public String getText() {
        return text;
    }
    
    public List<DialogueOption> getOptions() {
        return options;
    }
    
    public DialogueNode getNextNode() {
        return nextNode;
    }
    
    public String getPortraitPath() {
        return portraitPath;
    }
    
    public SpeakerType getSpeakerType() {
        return speakerType;
    }
    
    public boolean hasOptions() {
        return !options.isEmpty();
    }
    
    public boolean hasNext() {
        return nextNode != null || !options.isEmpty();
    }
    
    // ========== 内部类：对话选项 ==========
    
    public static class DialogueOption {
        private String text;
        private DialogueNode targetNode;
        
        public DialogueOption(String text, DialogueNode targetNode) {
            this.text = text;
            this.targetNode = targetNode;
        }
        
        public String getText() {
            return text;
        }
        
        public DialogueNode getTargetNode() {
            return targetNode;
        }
    }
    
    // ========== 静态工厂方法 ==========
    
    /**
     * 创建简单的线性对话链
     */
    public static DialogueNode createChain(String speaker, String... texts) {
        if (texts.length == 0) return null;
        
        DialogueNode first = new DialogueNode(speaker, texts[0]);
        DialogueNode current = first;
        
        for (int i = 1; i < texts.length; i++) {
            current = current.then(new DialogueNode(speaker, texts[i]));
        }
        
        return first;
    }
}
