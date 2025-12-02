package com.gameengine.components;

import com.gameengine.core.Component;
import com.gameengine.graphics.IRenderer;
import com.gameengine.math.Vector2;

/**
 * 渲染组件，负责对象的渲染
 */
public class RenderComponent extends Component<RenderComponent> {
    private IRenderer renderer;
    private RenderType renderType;
    private Vector2 size;
    private Color color;
    private boolean visible;
    private String imagePath;    // 图片路径
    private float rotation;      // 旋转角度（弧度）
    private float alpha;         // 透明度
    
    public enum RenderType {
        RECTANGLE,
        CIRCLE,
        LINE,
        IMAGE           // 图片渲染类型
    }
    
    public static class Color {
        public float r, g, b, a;
        
        public Color(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
        }
        
        public Color(float r, float g, float b) {
            this(r, g, b, 1.0f);
        }
    }
    
    public RenderComponent() {
        this.renderType = RenderType.RECTANGLE;
        this.size = new Vector2(20, 20);
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.visible = true;
        this.imagePath = null;
        this.rotation = 0.0f;
        this.alpha = 1.0f;
    }
    
    public RenderComponent(RenderType renderType, Vector2 size, Color color) {
        this.renderType = renderType;
        this.size = new Vector2(size);
        this.color = color;
        this.visible = true;
        this.imagePath = null;
        this.rotation = 0.0f;
        this.alpha = 1.0f;
    }
    
    /**
     * 创建图片渲染组件
     * @param imagePath 图片路径
     * @param size 渲染尺寸
     */
    public RenderComponent(String imagePath, Vector2 size) {
        this.renderType = RenderType.IMAGE;
        this.size = new Vector2(size);
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.visible = true;
        this.imagePath = imagePath;
        this.rotation = 0.0f;
        this.alpha = 1.0f;
    }
    
    /**
     * 创建图片渲染组件（带透明度和旋转）
     * @param imagePath 图片路径
     * @param size 渲染尺寸
     * @param alpha 透明度
     * @param rotation 旋转角度（弧度）
     */
    public RenderComponent(String imagePath, Vector2 size, float alpha, float rotation) {
        this.renderType = RenderType.IMAGE;
        this.size = new Vector2(size);
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.visible = true;
        this.imagePath = imagePath;
        this.rotation = rotation;
        this.alpha = alpha;
    }
    
    @Override
    public void initialize() {
        // 获取渲染器引用
        if (owner != null) {
            // 这里需要从游戏引擎获取渲染器
            // 在实际实现中，可以通过依赖注入或其他方式获取
        }
    }
    
    @Override
    public void update(float deltaTime) {
        // 渲染组件通常不需要每帧更新
    }
    
    @Override
    public void render() {
        if (!visible || renderer == null) {
            return;
        }
        
        TransformComponent transform = owner.getComponent(TransformComponent.class);
        if (transform == null) {
            return;
        }
        
        Vector2 position = transform.getPosition();
        
        switch (renderType) {
            case RECTANGLE:
                renderer.drawRect(position.x, position.y, size.x, size.y, 
                                color.r, color.g, color.b, color.a);
                break;
            case CIRCLE:
                renderer.drawCircle(position.x + size.x/2, position.y + size.y/2, 
                                  size.x/2, 16, color.r, color.g, color.b, color.a);
                break;
            case LINE:
                renderer.drawLine(position.x, position.y, 
                                position.x + size.x, position.y + size.y,
                                color.r, color.g, color.b, color.a);
                break;
            case IMAGE:
                if (imagePath != null) {
                    if (rotation != 0) {
                        // 带旋转的图片渲染（以中心点为旋转点）
                        renderer.drawImageRotated(imagePath, 
                                                 position.x + size.x/2, 
                                                 position.y + size.y/2, 
                                                 size.x, size.y, rotation, alpha);
                    } else {
                        // 普通图片渲染
                        renderer.drawImage(imagePath, position.x, position.y, 
                                          size.x, size.y, alpha);
                    }
                }
                break;
        }
    }

    @Override
    public String record() {
        // 根据渲染类型记录不同的数据
        if (renderType == RenderType.IMAGE) {
            // 图片类型：记录图片路径、尺寸、旋转和透明度
            return String.format("%s|%.1f|%.1f|%.4f|%.4f",
                    imagePath != null ? imagePath : "",
                    size.x,
                    size.y,
                    rotation,
                    alpha);
        } else {
            // 其他类型：记录颜色信息
            return String.format("%s|%f|%f|%f|%f",
                    renderType.name(),
                    color.r,
                    color.g,
                    color.b,
                    color.a);
        }
    }
    
    /**
     * 设置渲染器
     */
    public void setRenderer(IRenderer renderer) {
        this.renderer = renderer;
    }
    
    /**
     * 设置颜色
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * 设置颜色
     */
    public void setColor(float r, float g, float b, float a) {
        this.color = new Color(r, g, b, a);
    }
    
    /**
     * 设置大小
     */
    public void setSize(Vector2 size) {
        this.size = new Vector2(size);
    }
    
    /**
     * 设置可见性
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * 设置图片路径
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
        if (imagePath != null) {
            this.renderType = RenderType.IMAGE;
        }
    }
    
    /**
     * 设置旋转角度
     * @param rotation 旋转角度（弧度）
     */
    public void setRotation(float rotation) {
        this.rotation = rotation;
    }
    
    /**
     * 设置透明度
     * @param alpha 透明度 (0.0-1.0)
     */
    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }
    
    // 取值方法
    public RenderType getRenderType() {
        return renderType;
    }
    
    public Vector2 getSize() {
        return new Vector2(size);
    }
    
    public Color getColor() {
        return color;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public float getRotation() {
        return rotation;
    }
    
    public float getAlpha() {
        return alpha;
    }

    /**
     * 返回绑定到此渲染组件的渲染器实例（可能为 null）
     */
    public IRenderer getRenderer() {
        return this.renderer;
    }
}
