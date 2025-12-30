package com.gameengine.app.testutils;

import com.gameengine.core.GameEngine;
import com.gameengine.graphics.IRenderer;
import com.gameengine.input.InputManager;

import java.lang.reflect.Field;

public class TestUtils {
    private static sun.misc.Unsafe unsafe;

    static {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (sun.misc.Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static GameEngine createEngineWith(IRenderer renderer) {
        try {
            GameEngine engine = (GameEngine) unsafe.allocateInstance(GameEngine.class);

            // set private fields
            setField(engine, "renderer", renderer);
            setField(engine, "inputManager", InputManager.getInstance());
            setField(engine, "running", false);

            return engine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static GameEngine createEngineWith(IRenderer renderer, InputManager inputManager) {
        try {
            GameEngine engine = (GameEngine) unsafe.allocateInstance(GameEngine.class);

            // set private fields
            setField(engine, "renderer", renderer);
            setField(engine, "inputManager", inputManager == null ? InputManager.getInstance() : inputManager);
            setField(engine, "running", false);

            return engine;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = null;
        Class<?> cls = target.getClass();
        while (cls != null) {
            try {
                f = cls.getDeclaredField(name);
                break;
            } catch (NoSuchFieldException ex) {
                cls = cls.getSuperclass();
            }
        }
        if (f == null) throw new NoSuchFieldException(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
