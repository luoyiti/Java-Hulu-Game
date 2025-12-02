package com.gameengine.graphics;

public interface IRenderer {
    void beginFrame();
    void endFrame();
    
    void drawRect(float x, float y, float width, float height, float r, float g, float b, float a);
    void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a);
    void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a);
    void drawText(String text, float x, float y, float size, float r, float g, float b, float a);
    void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth);
    
    /**
     * 绘制图片
     * @param imagePath 图片路径（相对于 resources 目录或绝对路径）
     * @param x 绘制位置左上角 x 坐标
     * @param y 绘制位置左上角 y 坐标
     * @param width 绘制宽度
     * @param height 绘制高度
     * @param alpha 透明度 (0.0-1.0)
     */
    void drawImage(String imagePath, float x, float y, float width, float height, float alpha);
    
    /**
     * 绘制图片（带旋转）
     * @param imagePath 图片路径（相对于 resources 目录或绝对路径）
     * @param x 绘制位置中心 x 坐标
     * @param y 绘制位置中心 y 坐标
     * @param width 绘制宽度
     * @param height 绘制高度
     * @param rotation 旋转角度（弧度）
     * @param alpha 透明度 (0.0-1.0)
     */
    void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha);
    
    boolean shouldClose();
    void pollEvents();
    void cleanup();
    
    int getWidth();
    int getHeight();
    String getTitle();
}

