package com.gameengine.net;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * ReactorType枚举类的测试用例
 * 测试ReactorType枚举值的定义和使用
 */
public class ReactorTypeTest {
    
    /**
     * 测试MAIN枚举值
     */
    @Test
    public void testMain() {
        ReactorType main = ReactorType.MAIN;
        assertNotNull("MAIN枚举值应该存在", main);
        assertEquals("MAIN枚举值名称应该正确", "MAIN", main.name());
    }
    
    /**
     * 测试SUB枚举值
     */
    @Test
    public void testSub() {
        ReactorType sub = ReactorType.SUB;
        assertNotNull("SUB枚举值应该存在", sub);
        assertEquals("SUB枚举值名称应该正确", "SUB", sub.name());
    }
    
    /**
     * 测试所有枚举值
     */
    @Test
    public void testAllValues() {
        ReactorType[] values = ReactorType.values();
        assertEquals("应该有2个枚举值", 2, values.length);
        
        boolean hasMain = false;
        boolean hasSub = false;
        
        for (ReactorType type : values) {
            if (type == ReactorType.MAIN) {
                hasMain = true;
            } else if (type == ReactorType.SUB) {
                hasSub = true;
            }
        }
        
        assertTrue("应该包含MAIN枚举值", hasMain);
        assertTrue("应该包含SUB枚举值", hasSub);
    }
    
    /**
     * 测试valueOf方法
     */
    @Test
    public void testValueOf() {
        ReactorType main = ReactorType.valueOf("MAIN");
        assertEquals("valueOf应该返回MAIN", ReactorType.MAIN, main);
        
        ReactorType sub = ReactorType.valueOf("SUB");
        assertEquals("valueOf应该返回SUB", ReactorType.SUB, sub);
    }
    
    /**
     * 测试valueOf无效值
     */
    @Test(expected = IllegalArgumentException.class)
    public void testValueOfInvalid() {
        ReactorType.valueOf("INVALID");
    }
    
    /**
     * 测试枚举值的比较
     */
    @Test
    public void testEnumComparison() {
        ReactorType main1 = ReactorType.MAIN;
        ReactorType main2 = ReactorType.MAIN;
        ReactorType sub = ReactorType.SUB;
        
        // 同一枚举值应该相等
        assertEquals("同一枚举值应该相等", main1, main2);
        assertTrue("同一枚举值应该相等", main1 == main2);
        
        // 不同枚举值应该不相等
        assertNotEquals("不同枚举值应该不相等", main1, sub);
        assertFalse("不同枚举值应该不相等", main1 == sub);
    }
    
    /**
     * 测试枚举值的ordinal
     */
    @Test
    public void testOrdinal() {
        ReactorType[] values = ReactorType.values();
        for (int i = 0; i < values.length; i++) {
            assertEquals("ordinal应该与数组索引一致", i, values[i].ordinal());
        }
    }
    
    /**
     * 测试枚举值的toString
     */
    @Test
    public void testToString() {
        assertEquals("MAIN的toString应该返回名称", "MAIN", ReactorType.MAIN.toString());
        assertEquals("SUB的toString应该返回名称", "SUB", ReactorType.SUB.toString());
    }
}

