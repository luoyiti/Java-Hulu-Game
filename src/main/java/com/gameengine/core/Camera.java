package com.gameengine.core;

import com.gameengine.math.Vector2;

/**
 * 相机类，用于管理游戏视口和坐标转换
 * 支持跟随目标、平滑移动和视锥剔除
 */
public class Camera {
    // 相机在世界坐标系中的位置（相机中心点）
    private Vector2 position;

    // 视口尺寸（屏幕可见区域）
    private float viewportWidth;
    private float viewportHeight;

    // 世界地图尺寸
    private float worldWidth;
    private float worldHeight;

    // 平滑跟随参数
    private float smoothSpeed = 0.1f; // 0-1之间，值越大跟随越快

    /**
     * 创建相机
     * 
     * @param viewportWidth  视口宽度（屏幕宽度）
     * @param viewportHeight 视口高度（屏幕高度）
     * @param worldWidth     世界地图宽度
     * @param worldHeight    世界地图高度
     */
    public Camera(float viewportWidth, float viewportHeight, float worldWidth, float worldHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;

        // 初始位置在世界中心
        this.position = new Vector2(worldWidth / 2, worldHeight / 2);
    }

    /**
     * 获取相机位置
     */
    public Vector2 getPosition() {
        return new Vector2(position);
    }

    /**
     * 设置相机位置（立即移动）
     */
    public void setPosition(Vector2 newPosition) {
        this.position = new Vector2(newPosition);
        clampPosition();
    }

    /**
     * 设置平滑跟随速度
     * 
     * @param speed 0-1之间，0最慢，1最快（立即跟随）
     */
    public void setSmoothSpeed(float speed) {
        this.smoothSpeed = Math.max(0, Math.min(1, speed));
    }

    /**
     * 平滑跟随目标位置
     * 
     * @param targetPosition 目标位置（世界坐标）
     * @param deltaTime      帧时间间隔
     */
    public void follow(Vector2 targetPosition, float deltaTime) {
        if (targetPosition == null)
            return;

        // 使用线性插值实现平滑跟随
        float t = 1.0f - (float) Math.pow(1.0f - smoothSpeed, deltaTime * 60.0f);

        position.x = lerp(position.x, targetPosition.x, t);
        position.y = lerp(position.y, targetPosition.y, t);

        clampPosition();
    }

    /**
     * 立即跟随目标（无平滑效果）
     */
    public void followImmediate(Vector2 targetPosition) {
        if (targetPosition == null)
            return;
        this.position = new Vector2(targetPosition);
        clampPosition();
    }

    /**
     * 限制相机位置在世界边界内
     */
    private void clampPosition() {
        float halfWidth = viewportWidth / 2;
        float halfHeight = viewportHeight / 2;

        // 如果世界比视口小，相机固定在世界中心
        if (worldWidth <= viewportWidth) {
            position.x = worldWidth / 2;
        } else {
            position.x = Math.max(halfWidth, Math.min(worldWidth - halfWidth, position.x));
        }

        if (worldHeight <= viewportHeight) {
            position.y = worldHeight / 2;
        } else {
            position.y = Math.max(halfHeight, Math.min(worldHeight - halfHeight, position.y));
        }
    }

    /**
     * 将世界坐标转换为屏幕坐标
     * 
     * @param worldPos 世界坐标
     * @return 屏幕坐标
     */
    public Vector2 worldToScreen(Vector2 worldPos) {
        if (worldPos == null)
            return new Vector2(0, 0);

        float screenX = worldPos.x - position.x + viewportWidth / 2;
        float screenY = worldPos.y - position.y + viewportHeight / 2;

        return new Vector2(screenX, screenY);
    }

    /**
     * 将屏幕坐标转换为世界坐标
     * 
     * @param screenPos 屏幕坐标
     * @return 世界坐标
     */
    public Vector2 screenToWorld(Vector2 screenPos) {
        if (screenPos == null)
            return new Vector2(0, 0);

        float worldX = screenPos.x + position.x - viewportWidth / 2;
        float worldY = screenPos.y + position.y - viewportHeight / 2;

        return new Vector2(worldX, worldY);
    }

    /**
     * 判断世界坐标中的矩形是否在视口内（可见）
     * 
     * @param worldPos 矩形中心的世界坐标
     * @param width    矩形宽度
     * @param height   矩形高度
     * @return 是否可见
     */
    public boolean isVisible(Vector2 worldPos, float width, float height) {
        if (worldPos == null)
            return false;

        // 计算相机视口在世界坐标中的范围
        float camLeft = position.x - viewportWidth / 2;
        float camRight = position.x + viewportWidth / 2;
        float camTop = position.y - viewportHeight / 2;
        float camBottom = position.y + viewportHeight / 2;

        // 计算物体在世界坐标中的范围
        float objLeft = worldPos.x - width / 2;
        float objRight = worldPos.x + width / 2;
        float objTop = worldPos.y - height / 2;
        float objBottom = worldPos.y + height / 2;

        // AABB碰撞检测
        return !(objRight < camLeft || objLeft > camRight ||
                objBottom < camTop || objTop > camBottom);
    }

    /**
     * 判断点是否在视口内
     */
    public boolean isPointVisible(Vector2 worldPos) {
        return isVisible(worldPos, 0, 0);
    }

    /**
     * 线性插值辅助函数
     */
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    // Getters
    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public float getWorldWidth() {
        return worldWidth;
    }

    public float getWorldHeight() {
        return worldHeight;
    }
}
