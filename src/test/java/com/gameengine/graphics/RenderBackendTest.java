package com.gameengine.graphics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * RenderBackend 枚举测试类
 */
public class RenderBackendTest {
    
    @Test
    public void testGpuBackendExists() {
        assertNotNull(RenderBackend.GPU);
    }
    
    @Test
    public void testValuesCount() {
        RenderBackend[] values = RenderBackend.values();
        assertEquals(1, values.length);
    }
    
    @Test
    public void testValueOf() {
        RenderBackend backend = RenderBackend.valueOf("GPU");
        assertEquals(RenderBackend.GPU, backend);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidValueOf() {
        RenderBackend.valueOf("INVALID");
    }
    
    @Test
    public void testName() {
        assertEquals("GPU", RenderBackend.GPU.name());
    }
    
    @Test
    public void testOrdinal() {
        assertEquals(0, RenderBackend.GPU.ordinal());
    }
}
