package com.gameengine.net;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.gameengine.game.Record;

import com.google.gson.Gson;

public final class NetworkBuffer {
    private static final Deque<Keyframe> buffer = new ArrayDeque<>();
    private static final Object lock = new Object();
    private static final double MAX_AGE_SEC = 2.0;
    private static final double INTERP_DELAY_SEC = 0.12; // 120ms 缓冲
    private static final Gson gson = new Gson();

    public static class Entity {
        public String id; public float x; public float y;
    }
    public static class Keyframe {
        public double t;
        public List<Entity> entities = new ArrayList<>();
    }

    public static void push(Keyframe kf) {
        synchronized (lock) {
            buffer.addLast(kf);
            // 修剪老帧
            double now = kf.t;
            while (!buffer.isEmpty() && now - buffer.peekFirst().t > MAX_AGE_SEC) buffer.pollFirst();
        }
    }

    public static Record parseJsonLine(String json) {
        // 解析 JSON
        Record record = gson.fromJson(json, Record.class);
        if (record == null || !"object_move".equals(record.getType())) return null;

        return record;
    }

    /**
     * 将服务端广播的 Record 推入插值缓冲区
     */
    public static void pushRecord(Record record) {
        if (record == null || record.getGameObjectsMove() == null) return;
        if (!"object_move".equals(record.getType())) return;

        Keyframe kf = new Keyframe();
        kf.t = System.currentTimeMillis() / 1000.0;
        record.getGameObjectsMove().forEach(obj -> {
            if (obj == null || obj.id == null) return;
            Entity e = new Entity();
            e.id = obj.id;
            e.x = obj.x;
            e.y = obj.y;
            kf.entities.add(e);
        });
        if (!kf.entities.isEmpty()) {
            push(kf);
        }
    }

    public static Map<String, float[]> sample() {
        double now = System.currentTimeMillis() / 1000.0;
        double target = now - INTERP_DELAY_SEC; // 插值点
        
        Keyframe a = null, b = null;
        synchronized (lock) {
            if (buffer.isEmpty()) return new HashMap<>();
            a = buffer.peekFirst();
            b = buffer.peekLast();
            for (Keyframe k : buffer) {
                if (k.t <= target) a = k; else { b = k; break; }
            }
        }
        if (a == null) return new HashMap<>();
        if (b == null) b = a;
        double span = Math.max(1e-6, b.t - a.t);
        double u = Math.max(0.0, Math.min(1.0, (target - a.t) / span));
        Map<String, float[]> out = new HashMap<>();
        int n = Math.min(a.entities.size(), b.entities.size());
        for (int i = 0; i < n; i++) {
            Entity ea = a.entities.get(i);
            Entity eb = b.entities.get(i);
            if (ea == null || eb == null || ea.id == null) continue;
            float x = (float)((1.0 - u) * ea.x + u * eb.x);
            float y = (float)((1.0 - u) * ea.y + u * eb.y);
            out.put(ea.id, new float[]{x, y});
        }
        return out;
    }
}


