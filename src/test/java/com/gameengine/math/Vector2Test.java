package com.gameengine.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Vector2Test {
    
    @Test
    public void testAdd() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(3, 4);
        Vector2 result = v1.add(v2);
        assertEquals(new Vector2(4, 6), result);
        assertEquals(4, result.x, 0.000001);
        assertEquals(6, result.y, 0.000001);
    }

    @Test
    public void testSubtract() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(3, 4);
        Vector2 result = v1.subtract(v2);
        assertEquals(new Vector2(-2, -2), result);
        assertEquals(-2, result.x, 0.000001);
        assertEquals(-2, result.y, 0.000001);
    }

    @Test
    public void testMultiply() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 result = v1.multiply(3);
        assertEquals(new Vector2(3, 6), result);
        assertEquals(3, result.x, 0.000001);
        assertEquals(6, result.y, 0.000001);
    }
    
    @Test
    public void testMagnitude() {
        Vector2 v1 = new Vector2(3, 4);
        assertEquals(5, v1.magnitude(), 0.000001);
    }
    
    @Test
    public void testNormalize() {
        Vector2 v1 = new Vector2(3, 4);
        assertEquals(0.6, v1.normalize().x, 0.000001);
        assertEquals(0.8, v1.normalize().y, 0.000001);
    }
    
    @Test
    public void testDot() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(3, 4);
        assertEquals(11, v1.dot(v2), 0.000001);
    }
    
    @Test
    public void testDistance() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(3, 4);
        assertEquals(Math.sqrt(8), v1.distance(v2), 0.000001);
    }
    
    @Test
    public void testAngle() {
        Vector2 v1 = new Vector2(1, 0);
        assertEquals(0, v1.angle(), 0.000001);
        Vector2 v2 = new Vector2(0, 1);
        assertEquals(Math.PI / 2, v2.angle(), 0.000001);
        Vector2 v3 = new Vector2(-1, 0);
        assertEquals(Math.PI, v3.angle(), 0.000001);
        Vector2 v4 = new Vector2(0, -1);
        assertEquals(3 * Math.PI / 2, v4.angle(), 0.000001);
    }

    @Test
    public void testToString() {
        Vector2 v1 = new Vector2(1, 2);
        assertEquals("Vector2(1.00, 2.00)", v1.toString());
    }

    @Test
    public void testEquals() {
        Vector2 v1 = new Vector2(1, 2);
        Vector2 v2 = new Vector2(1, 2);
        assertEquals(v1, v2);
    }
}
