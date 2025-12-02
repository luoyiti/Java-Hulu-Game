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

    public LifeFeatureComponent(int blood) {
        this.blood = blood;
        this.maxBlood = blood;
        this.lifetime = 0;
        this.maxLifetime = -1; // 默认无限生命周期
        this.hasLifetime = false;
        this.isunbeatable = false; // 默认不是无敌的，即均可受到伤害
    }
    
    /**
     * 设置生命周期（用于技能等临时对象）
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
    }

    @Override
    public void render() {
        // 统一由生命组件负责渲染血条。
        if (owner == null) return;

        // 获取坐标
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform == null) return;

        // 获取渲染组件并从中取得渲染器与尺寸信息
        RenderComponent renderComp = owner.getComponent(RenderComponent.class);
        com.gameengine.graphics.IRenderer renderer = null;
        float barWidth = 30f;
        float barHeight = 4f;
        float offsetY = -10f;

        if (renderComp != null) {
            renderer = renderComp.getRenderer();
            try {
                barWidth = renderComp.getSize().x;
            } catch (Exception e) {
                // ignore and use default
            }
        }

        if (renderer == null) return; // 无渲染器则无法绘制

        // 计算血条位置（默认位于物体上方）
        com.gameengine.math.Vector2 p = transform.getPosition();
        float x = p.x;
        float y = p.y + offsetY;

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

}