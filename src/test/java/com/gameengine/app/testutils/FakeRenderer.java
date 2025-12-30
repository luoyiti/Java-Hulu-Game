package com.gameengine.app.testutils;

import com.gameengine.graphics.IRenderer;

import java.util.ArrayList;
import java.util.List;

public class FakeRenderer implements IRenderer {
    public static class DrawCall {
        public final String path;
        public final float x, y, w, h, a;
        public DrawCall(String path, float x, float y, float w, float h, float a) {
            this.path = path; this.x = x; this.y = y; this.w = w; this.h = h; this.a = a;
        }
    }

    public static class TextCall {
        public final String text;
        public final float x, y, size, r, g, b, a;
        public TextCall(String text, float x, float y, float size, float r, float g, float b, float a) {
            this.text = text; this.x = x; this.y = y; this.size = size; this.r = r; this.g = g; this.b = b; this.a = a;
        }
    }

    public final List<DrawCall> drawCalls = new ArrayList<>();
    public final List<TextCall> textCalls = new ArrayList<>();
    private final int width;
    private final int height;

    public FakeRenderer() {
        this(800, 600);
    }

    public FakeRenderer(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void beginFrame() {}

    @Override
    public void endFrame() {}

    @Override
    public void drawRect(float x, float y, float width, float height, float r, float g, float b, float a) {}

    @Override
    public void drawCircle(float x, float y, float radius, int segments, float r, float g, float b, float a) {}

    @Override
    public void drawLine(float x1, float y1, float x2, float y2, float r, float g, float b, float a) {}

    @Override
    public void drawHealthBar(float x, float y, float width, float height, int currentHealth, int maxHealth) {}

    @Override
    public void drawImage(String imagePath, float x, float y, float width, float height, float alpha) {
        drawCalls.add(new DrawCall(imagePath, x, y, width, height, alpha));
    }

    @Override
    public void drawText(String text, float x, float y, float size, float r, float g, float b, float a) {
        textCalls.add(new TextCall(text, x, y, size, r, g, b, a));
    }

    @Override
    public void drawImageRotated(String imagePath, float x, float y, float width, float height, float rotation, float alpha) {}

    @Override
    public boolean shouldClose() { return false; }

    @Override
    public void pollEvents() {}

    @Override
    public void cleanup() {}

    @Override
    public int getWidth() { return width; }

    @Override
    public int getHeight() { return height; }

    @Override
    public String getTitle() { return "Fake"; }
}
