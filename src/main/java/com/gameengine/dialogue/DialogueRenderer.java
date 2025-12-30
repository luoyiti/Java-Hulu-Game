package com.gameengine.dialogue;

import com.gameengine.graphics.IRenderer;

import java.util.List;

/**
 * 对话渲染器
 * 负责绘制对话框、文本和头像
 * 支持玩家头像在左边、怪物头像在右边的交互对话效果
 */
public class DialogueRenderer {
    private IRenderer renderer;
    
    // 对话框尺寸和位置
    private float boxX;
    private float boxY;
    private float boxWidth;
    private float boxHeight;
    private float padding = 20f;
    
    // 头像尺寸
    private float portraitSize = 100f;
    private float portraitPadding = 15f;
    
    // 颜色配置
    private float[] bgColor = {0.08f, 0.08f, 0.12f, 0.95f};      // 深蓝黑色背景
    private float[] borderColor = {0.7f, 0.55f, 0.2f, 1.0f};     // 金色边框
    private float[] innerBorderColor = {0.5f, 0.4f, 0.15f, 0.6f}; // 内边框
    private float[] textColor = {1.0f, 1.0f, 1.0f, 1.0f};        // 白色文字
    private float[] playerNameColor = {0.4f, 0.8f, 1.0f, 1.0f};  // 蓝色玩家名称
    private float[] enemyNameColor = {1.0f, 0.4f, 0.4f, 1.0f};   // 红色敌人名称
    private float[] narratorNameColor = {1.0f, 0.85f, 0.4f, 1.0f}; // 金色旁白
    private float[] hintColor = {0.5f, 0.5f, 0.5f, 1.0f};        // 灰色提示文字
    private float[] optionColor = {0.8f, 0.9f, 1.0f, 1.0f};      // 淡蓝色选项
    
    // 装饰颜色
    private float[] cornerColor = {0.8f, 0.6f, 0.2f, 0.8f};      // 角落装饰
    private float[] glowColor = {0.3f, 0.25f, 0.1f, 0.3f};       // 发光效果

    // 字体大小配置（可调整）
    private float dialogueTextSize = 28f;
    private float nameTextSize = 24f;
    private float hintTextSize = 18f;
    private float optionTextSize = 20f;
    
    public DialogueRenderer(IRenderer renderer) {
        this.renderer = renderer;
        updateBoxDimensions();
    }
    
    /**
     * 更新对话框尺寸（基于屏幕大小）
     */
    private void updateBoxDimensions() {
        float screenWidth = renderer.getWidth();
        float screenHeight = renderer.getHeight();
        
        boxWidth = screenWidth * 0.70f;  // 缩小宽度给头像留位置
        boxHeight = 160f;
        boxX = (screenWidth - boxWidth) / 2f;
        boxY = screenHeight - boxHeight - 25f;
    }
    
    /**
     * 渲染对话框
     */
    public void render(DialogueNode node, String displayedText, boolean textComplete) {
        if (node == null) return;
        
        updateBoxDimensions();
        
        // 绘制半透明全屏遮罩
            renderer.drawRect(0, 0, renderer.getWidth(), renderer.getHeight(), 
                     0f, 0f, 0f, 0.4f);
        
        // 根据说话者类型决定布局
        DialogueNode.SpeakerType type = node.getSpeakerType();
        
        // 绘制对话框背景和装饰
        drawDialogueBox(type);
        
        // 绘制头像
        drawPortraits(node);
        
        // 绘制说话者名称
        drawSpeakerName(node.getSpeaker(), type);
        
        // 绘制对话文本
        drawText(displayedText, type);
        
        // 绘制选项或继续提示
        if (textComplete) {
            if (node.hasOptions()) {
                drawOptions(node.getOptions());
            } else {
                drawContinueHint();
            }
        }
    }
    
    /**
     * 绘制对话框背景和装饰
     */
    private void drawDialogueBox(DialogueNode.SpeakerType type) {
        // 外发光效果
        float glowSize = 8f;
        renderer.drawRect(boxX - glowSize, boxY - glowSize, 
                         boxWidth + glowSize * 2, boxHeight + glowSize * 2,
                         glowColor[0], glowColor[1], glowColor[2], glowColor[3]);
        
        // 外边框
        float borderWidth = 3f;
        renderer.drawRect(boxX - borderWidth, boxY - borderWidth, 
                         boxWidth + borderWidth * 2, boxHeight + borderWidth * 2,
                         borderColor[0], borderColor[1], borderColor[2], borderColor[3]);
        
        // 主背景
        renderer.drawRect(boxX, boxY, boxWidth, boxHeight,
                         bgColor[0], bgColor[1], bgColor[2], bgColor[3]);
        
        // 内边框装饰
        float innerMargin = 5f;
        renderer.drawRect(boxX + innerMargin, boxY + innerMargin, 
                         boxWidth - innerMargin * 2, boxHeight - innerMargin * 2,
                         innerBorderColor[0], innerBorderColor[1], innerBorderColor[2], 0.3f);
        
        // 顶部装饰条
        float topBarHeight = 4f;
        float[] barColor = getTypeColor(type);
        renderer.drawRect(boxX + 10, boxY + 8, boxWidth - 20, topBarHeight,
                         barColor[0], barColor[1], barColor[2], 0.7f);
        
        // 角落装饰
        drawCornerDecorations();
        
        // 底部分隔线
        renderer.drawRect(boxX + padding, boxY + boxHeight - 35, 
                         boxWidth - padding * 2, 1f,
                         innerBorderColor[0], innerBorderColor[1], innerBorderColor[2], 0.5f);
    }
    
    /**
     * 绘制角落装饰
     */
    private void drawCornerDecorations() {
        float cornerSize = 12f;
        float offset = 3f;
        
        // 左上角
        renderer.drawRect(boxX + offset, boxY + offset, cornerSize, 2f,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        renderer.drawRect(boxX + offset, boxY + offset, 2f, cornerSize,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        
        // 右上角
        renderer.drawRect(boxX + boxWidth - cornerSize - offset, boxY + offset, cornerSize, 2f,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        renderer.drawRect(boxX + boxWidth - offset - 2f, boxY + offset, 2f, cornerSize,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        
        // 左下角
        renderer.drawRect(boxX + offset, boxY + boxHeight - offset - 2f, cornerSize, 2f,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        renderer.drawRect(boxX + offset, boxY + boxHeight - cornerSize - offset, 2f, cornerSize,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        
        // 右下角
        renderer.drawRect(boxX + boxWidth - cornerSize - offset, boxY + boxHeight - offset - 2f, cornerSize, 2f,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
        renderer.drawRect(boxX + boxWidth - offset - 2f, boxY + boxHeight - cornerSize - offset, 2f, cornerSize,
                         cornerColor[0], cornerColor[1], cornerColor[2], cornerColor[3]);
    }
    
    /**
     * 绘制头像（玩家在左，怪物在右）
     */
    private void drawPortraits(DialogueNode node) {
        String portraitPath = node.getPortraitPath();
        DialogueNode.SpeakerType type = node.getSpeakerType();
        
        if (portraitPath == null || portraitPath.isEmpty()) return;
        
        float portraitX, portraitY;
        portraitY = boxY + (boxHeight - portraitSize) / 2f;
        
        // 根据说话者类型决定头像位置
        if (type == DialogueNode.SpeakerType.PLAYER) {
            // 玩家头像在左边
            portraitX = boxX - portraitSize - portraitPadding;
            drawPortraitFrame(portraitX, portraitY, playerNameColor);
        } else if (type == DialogueNode.SpeakerType.ENEMY) {
            // 怪物头像在右边
            portraitX = boxX + boxWidth + portraitPadding;
            drawPortraitFrame(portraitX, portraitY, enemyNameColor);
        } else {
            // 旁白无头像
            return;
        }
        
        // 绘制头像图片
        renderer.drawImage(portraitPath, portraitX, portraitY, portraitSize, portraitSize, 1.0f);
    }
    
    /**
     * 绘制头像边框
     */
    private void drawPortraitFrame(float x, float y, float[] color) {
        float frameWidth = 4f;
        float glowWidth = 6f;
        
        // 发光效果
        renderer.drawRect(x - glowWidth, y - glowWidth, 
                         portraitSize + glowWidth * 2, portraitSize + glowWidth * 2,
                         color[0] * 0.3f, color[1] * 0.3f, color[2] * 0.3f, 0.4f);
        
        // 外边框
        renderer.drawRect(x - frameWidth, y - frameWidth, 
                         portraitSize + frameWidth * 2, portraitSize + frameWidth * 2,
                         color[0], color[1], color[2], 0.9f);
        
        // 背景（用于没有图片时）
        renderer.drawRect(x, y, portraitSize, portraitSize,
                         0.1f, 0.1f, 0.15f, 1.0f);
        
        // 内边框装饰
        renderer.drawRect(x + 2, y + 2, portraitSize - 4, portraitSize - 4,
                         color[0], color[1], color[2], 0.3f);
    }
    
    /**
     * 绘制说话者名称
     */
    private void drawSpeakerName(String speaker, DialogueNode.SpeakerType type) {
        if (speaker == null || speaker.isEmpty()) return;
        
        float[] nameColor = getTypeColor(type);
        float nameX, nameY;
        nameY = boxY + 22f;

        float approxCharWidth = nameTextSize * 0.6f; // 估算字符宽度
        // 根据说话者类型决定名称位置
        if (type == DialogueNode.SpeakerType.PLAYER) {
            nameX = boxX + padding;
        } else if (type == DialogueNode.SpeakerType.ENEMY) {
            // 敌人名称在右边
            float nameWidth = speaker.length() * approxCharWidth + 30f;
            nameX = boxX + boxWidth - nameWidth - padding;
        } else {
            // 旁白居中
            float nameWidth = speaker.length() * approxCharWidth;
            nameX = boxX + (boxWidth - nameWidth) / 2f;
        }

        // 绘制名称背景框
        float bgWidth = speaker.length() * approxCharWidth + 24f;
        float bgHeight = nameTextSize + 6f;

        // 名称背景
        renderer.drawRect(nameX - 8f, nameY - 4f, bgWidth, bgHeight,
                         0.15f, 0.12f, 0.08f, 0.85f);

        // 名称背景边框
        renderer.drawRect(nameX - 8f, nameY - 4f, bgWidth, 2f,
                         nameColor[0], nameColor[1], nameColor[2], 0.6f);

        // 绘制名称文字
        renderer.drawText(speaker, nameX, nameY,
                         nameTextSize,
                         nameColor[0], nameColor[1], nameColor[2], nameColor[3]);
    }
    
    /**
     * 获取说话者类型对应的颜色
     */
    private float[] getTypeColor(DialogueNode.SpeakerType type) {
        switch (type) {
            case PLAYER:
                return playerNameColor;
            case ENEMY:
                return enemyNameColor;
            case NARRATOR:
            default:
                return narratorNameColor;
        }
    }
    
    /**
     * 绘制对话文本
     */
    private void drawText(String text, DialogueNode.SpeakerType type) {
        if (text == null || text.isEmpty()) return;
        
        float textX = boxX + padding + 5f;
        float textY = boxY + 60f;
        float maxWidth = boxWidth - padding * 2 - 10f;

        // 简单的自动换行，使用对话字体的估算字符宽度
        float charWidthEstimate = dialogueTextSize * 0.6f;
        String[] lines = wrapText(text, maxWidth, charWidthEstimate);
        float lineHeight = dialogueTextSize * 1.3f;

        for (int i = 0; i < lines.length && i < 3; i++) {
            renderer.drawText(lines[i], textX, textY + i * lineHeight,
                             dialogueTextSize,
                             textColor[0], textColor[1], textColor[2], textColor[3]);
        }
    }
    
    /**
     * 绘制继续提示
     */
    private void drawContinueHint() {
        String hint = "▼ 按 空格键 继续";
        float hintX = boxX + boxWidth - 175f;
        float hintY = boxY + boxHeight - 22f;
        
        renderer.drawText(hint, hintX, hintY,
                 hintTextSize,
                 hintColor[0], hintColor[1], hintColor[2], hintColor[3]);
    }
    
    /**
     * 绘制选项
     */
    private void drawOptions(List<DialogueNode.DialogueOption> options) {
        float optionX = boxX + padding;
        float optionY = boxY + boxHeight + 15f;
        float optionHeight = 32f;
        
        // 绘制选项背景
        float optionBgHeight = options.size() * optionHeight + 20f;
        
        // 背景外框
        renderer.drawRect(optionX - 12f, optionY - 12f, 
                         boxWidth - padding + 4f, optionBgHeight + 4f,
                         borderColor[0], borderColor[1], borderColor[2], 0.8f);
        
        // 背景
        renderer.drawRect(optionX - 10f, optionY - 10f, 
                         boxWidth - padding, optionBgHeight,
                         bgColor[0], bgColor[1], bgColor[2], 0.95f);
        
        for (int i = 0; i < options.size(); i++) {
            String optionText = (i + 1) + ". " + options.get(i).getText();
            
            // 选项前的装饰点
            renderer.drawRect(optionX, optionY + i * optionHeight + 8f, 6f, 6f,
                             optionColor[0], optionColor[1], optionColor[2], 0.8f);
            
            renderer.drawText(optionText, optionX + 15f, optionY + i * optionHeight,
                             optionTextSize,
                             optionColor[0], optionColor[1], optionColor[2], optionColor[3]);
        }
    }
    
    /**
     * 简单的文本换行
     */
    private String[] wrapText(String text, float maxWidth, float charWidth) {
        int charsPerLine = (int)(maxWidth / charWidth);
        if (charsPerLine <= 0) charsPerLine = 25;
        
        java.util.List<String> lines = new java.util.ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            currentLine.append(c);
            // 中文字符占更多宽度
            int effectiveLength = 0;
            for (char ch : currentLine.toString().toCharArray()) {
                effectiveLength += (ch > 127) ? 2 : 1;
            }
            if (effectiveLength >= charsPerLine) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }
        }
        
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }
        
        return lines.toArray(new String[0]);
    }
    
    // ========== 颜色配置方法 ==========
    
    public void setBackgroundColor(float r, float g, float b, float a) {
        bgColor = new float[]{r, g, b, a};
    }
    
    public void setBorderColor(float r, float g, float b, float a) {
        borderColor = new float[]{r, g, b, a};
    }
    
    public void setTextColor(float r, float g, float b, float a) {
        textColor = new float[]{r, g, b, a};
    }
    
    public void setPlayerNameColor(float r, float g, float b, float a) {
        playerNameColor = new float[]{r, g, b, a};
    }
    
    public void setEnemyNameColor(float r, float g, float b, float a) {
        enemyNameColor = new float[]{r, g, b, a};
    }
}
