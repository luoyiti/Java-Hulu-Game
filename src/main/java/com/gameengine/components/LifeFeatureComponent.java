package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.math.Vector2;

public class LifeFeatureComponent extends Component<LifeFeatureComponent> {
    /**
     * 生命属性组件，用于定义实体的生命属性信息
     * 例如其攻击力，生命值，攻击的生命周期等
     */
    public int blood;
    private float lifetime; // 生命周期计时器
    private float maxLifetime; // 最大生命周期（-1表示无限）
    private boolean hasLifetime; // 是否启用生命周期
    public boolean isunbeatable; // 是否是无敌状态
    private int maxBlood;

    // 无敌帧相关字段
    private boolean invincible; // 是否处于无敌帧状态
    private float invincibleTimer; // 无敌帧计时器
    private float invincibleDuration; // 无敌帧持续时间
    private float blinkTimer; // 闪烁计时器
    private float blinkInterval; // 闪烁间隔

    public LifeFeatureComponent(int blood) {
        this.blood = blood;
        this.maxBlood = blood;
        this.lifetime = 0;
        this.maxLifetime = -1; // 默认无限生命周期
        this.hasLifetime = false;
        this.isunbeatable = false; // 默认不是无敌的，即均可受到伤害

        // 初始化无敌帧相关字段
        this.invincible = false;
        this.invincibleTimer = 0;
        this.invincibleDuration = 1.0f; // 默认1秒无敌时间
        this.blinkTimer = 0;
        this.blinkInterval = 0.1f; // 每0.1秒闪烁一次
    }

    /**
     * 设置生命周期（用于技能等临时对象）
     * 
     * @param maxLifetime 最大生命周期（秒）
     */
    public void setLifetime(float maxLifetime) {
        this.maxLifetime = maxLifetime;
        this.hasLifetime = true;
        this.lifetime = 0;
    }

    /**
     * 重置生命周期计时器
     */
    public void resetLifetime() {
        this.lifetime = 0;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void update(float deltaTime) {
        // 如果启用了生命周期，则更新计时器
        if (hasLifetime && maxLifetime > 0) {
            lifetime += deltaTime;

            // 生命周期结束，将对象移到屏幕外
            if (lifetime >= maxLifetime) {
                TransformComponent transform = owner.getComponent(TransformComponent.class);
                PhysicsComponent physics = owner.getComponent(PhysicsComponent.class);

                if (transform != null) {
                    transform.setPosition(new Vector2(-1000, -1000));
                }
                if (physics != null) {
                    physics.setVelocity(new Vector2(0, 0));
                }

                // 重置生命周期以便下次使用
                lifetime = 0;
            }
        }

        // 更新无敌帧计时器
        if (invincible) {
            invincibleTimer += deltaTime;
            blinkTimer += deltaTime;

            // 无敌时间结束
            if (invincibleTimer >= invincibleDuration) {
                invincible = false;
                invincibleTimer = 0;
                blinkTimer = 0;

                // 确保对象可见
                RenderComponent render = owner.getComponent(RenderComponent.class);
                if (render != null) {
                    render.setVisible(true);
                }
            }
        }
    }

    @Override
    public void render() {
        // 统一由生命组件负责渲染血条。
        if (owner == null)
            return;

        // 处理无敌帧闪烁效果
        if (invincible) {
            RenderComponent render = owner.getComponent(RenderComponent.class);
            if (render != null) {
                // 根据闪烁计时器控制可见性
                // 每个闪烁周期显示/隐藏切换
                int blinkCycle = (int) (blinkTimer / blinkInterval);
                render.setVisible(blinkCycle % 2 == 0);
            }
        }

        // 获取坐标
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform == null)
            return;

        // 获取渲染组件并从中取得渲染器与尺寸信息
        RenderComponent renderComp = owner.getComponent(RenderComponent.class);
        com.gameengine.graphics.IRenderer renderer = null;
        float barWidth = 30f;
        float barHeight = 4f;
        float objectHeight = 30f; // 默认对象高度

        if (renderComp != null) {
            renderer = renderComp.getRenderer();
            try {
                Vector2 size = renderComp.getSize();
                barWidth = size.x;
                objectHeight = size.y; // 获取对象的实际高度
            } catch (Exception e) {
                // ignore and use default
            }
        }

        if (renderer == null)
            return; // 无渲染器则无法绘制

        // 计算血条位置
        // 由于position现在代表对象中心点，血条应该显示在对象顶部上方
        // 血条位置 = 中心点Y - 对象高度的一半 - 血条高度 - 间距
        com.gameengine.math.Vector2 worldPos = transform.getPosition();
        com.gameengine.math.Vector2 screenPos = worldPos;

        // 如果存在相机，将世界坐标转换为屏幕坐标
        if (owner.getScene() != null && owner.getScene().getCamera() != null) {
            com.gameengine.core.Camera camera = owner.getScene().getCamera();

            // 视锥剔除：只渲染可见的血条
            if (!camera.isVisible(worldPos, barWidth, barHeight)) {
                return;
            }

            // 转换为屏幕坐标
            screenPos = camera.worldToScreen(worldPos);
        }

        // 血条位置计算：
        // x坐标：对象中心点X - 血条宽度的一半（使血条居中对齐）
        // y坐标：对象中心点Y - 对象高度的一半 - 血条高度 - 5像素间距
        float x = screenPos.x - barWidth / 2.0f;
        float y = screenPos.y - objectHeight / 2.0f - barHeight - 5.0f;

        int current = this.blood;
        int max = this.maxBlood > 0 ? this.maxBlood : Math.max(1, current);

        renderer.drawHealthBar(x, y, barWidth, barHeight, current, max);
    }

    @Override
    public String record() {
        String recordLine = "";
        if (owner != null) {
            recordLine = String.format("LifeFeature:%d,%f;", blood, lifetime);
        }
        return recordLine;
    }

    public int getBlood() {
        return this.blood;
    }

    public float getLifetime() {
        return this.lifetime;
    }

    public boolean isLifetimeExpired() {
        return hasLifetime && maxLifetime > 0 && lifetime >= maxLifetime;
    }

    public int getMaxBlood() {
        return this.maxBlood;
    }

    /**
     * 触发无敌帧
     * 
     * @param duration 无敌持续时间（秒）
     */
    public void triggerInvincibility(float duration) {
        this.invincible = true;
        this.invincibleTimer = 0;
        this.invincibleDuration = duration;
        this.blinkTimer = 0;
    }

    /**
     * 触发无敌帧（使用默认1秒持续时间）
     */
    public void triggerInvincibility() {
        triggerInvincibility(1.0f);
    }

    /**
     * 检查是否处于无敌帧状态
     */
    public boolean isInvincible() {
        return this.invincible;
    }

    /**
     * 设置无敌帧持续时间
     */
    public void setInvincibilityDuration(float duration) {
        this.invincibleDuration = duration;
    }

}