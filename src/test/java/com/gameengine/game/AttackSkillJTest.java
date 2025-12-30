package com.gameengine.game;

import org.junit.Test;
import static org.junit.Assert.*;

import com.gameengine.components.TransformComponent;
import com.gameengine.math.Vector2;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/**
 * AttackSkillJ（玩家攻击技能J）测试类
 */
public class AttackSkillJTest {
    
    @Test
    public void testAttackSkillCreation() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        
        assertNotNull(skill);
        assertTrue(skill.getName().contains("Attacking SkillJ"));
    }
    
    @Test
    public void testAttackSkillWithIndex() {
        Random random = new Random(42);
        AttackSkillJ skill0 = new AttackSkillJ(0, null, random);
        AttackSkillJ skill1 = new AttackSkillJ(1, null, random);
        AttackSkillJ skill5 = new AttackSkillJ(5, null, random);
        
        assertTrue(skill0.getName().contains("0"));
        assertTrue(skill1.getName().contains("1"));
        assertTrue(skill5.getName().contains("5"));
    }
    
    @Test
    public void testSkillIdentity() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        
        // 测试身份设置
        assertEquals("Player Skill", skill.getidentity());
    }
    
    @Test
    public void testInitializePosition() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        
        Vector2 position = new Vector2(100, 200);
        skill.initializePosition(position, random);
        
        TransformComponent transform = skill.getComponent(TransformComponent.class);
        assertNotNull(transform);
        assertEquals(100, transform.getPosition().x, 0.001f);
        assertEquals(200, transform.getPosition().y, 0.001f);
    }
    
    @Test
    public void testUpdate() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        skill.initializePosition(new Vector2(100, 100), random);
        
        // update不应抛出异常
        skill.update(0.016f);
    }
    
    @Test
    public void testRender() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        
        // 没有渲染器时render不应抛出异常
        skill.render();
    }
    
    @Test
    public void testIsActiveByDefault() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        
        assertTrue(skill.isActive());
    }
    
    @Test
    public void testSetActive() {
        Random random = new Random(42);
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        
        skill.setActive(false);
        assertFalse(skill.isActive());
        
        skill.setActive(true);
        assertTrue(skill.isActive());
    }
    
    // ==================== 五行技能类型测试 ====================
    
    @Test
    public void testSkillTypeEnum() {
        // 测试五种技能类型存在
        AttackSkillJ.SkillType[] types = AttackSkillJ.SkillType.values();
        assertEquals(5, types.length);
        
        // 验证所有五行元素
        assertNotNull(AttackSkillJ.SkillType.METAL);  // 金
        assertNotNull(AttackSkillJ.SkillType.WOOD);   // 木
        assertNotNull(AttackSkillJ.SkillType.WATER);  // 水
        assertNotNull(AttackSkillJ.SkillType.FIRE);   // 火
        assertNotNull(AttackSkillJ.SkillType.EARTH);  // 土
    }
    
    @Test
    public void testSkillTypeChineseNames() {
        // 测试每种技能类型的中文名称
        assertEquals("金", AttackSkillJ.SkillType.METAL.getChineseName());
        assertEquals("木", AttackSkillJ.SkillType.WOOD.getChineseName());
        assertEquals("水", AttackSkillJ.SkillType.WATER.getChineseName());
        assertEquals("火", AttackSkillJ.SkillType.FIRE.getChineseName());
        assertEquals("土", AttackSkillJ.SkillType.EARTH.getChineseName());
    }
    
    @Test
    public void testSkillTypeImagePaths() {
        // 测试每种技能类型的图片路径包含正确的文件名
        assertTrue(AttackSkillJ.SkillType.METAL.getImagePath().contains("goldball.png"));
        assertTrue(AttackSkillJ.SkillType.WOOD.getImagePath().contains("treeball.png"));
        assertTrue(AttackSkillJ.SkillType.WATER.getImagePath().contains("iceball.png"));
        assertTrue(AttackSkillJ.SkillType.FIRE.getImagePath().contains("fireball2.png"));
        assertTrue(AttackSkillJ.SkillType.EARTH.getImagePath().contains("dirtball.png"));
    }
    
    @Test
    public void testRandomSkillType() {
        // 测试随机技能类型生成
        Random random = new Random(42);
        Set<AttackSkillJ.SkillType> generatedTypes = new HashSet<>();
        
        // 多次生成随机技能类型，应该能覆盖所有五种
        for (int i = 0; i < 100; i++) {
            AttackSkillJ.SkillType type = AttackSkillJ.SkillType.random(random);
            assertNotNull(type);
            generatedTypes.add(type);
        }
        
        // 经过100次随机，应该能生成所有5种技能类型
        assertEquals(5, generatedTypes.size());
    }
    
    @Test
    public void testSkillWithType() {
        Random random = new Random(42);
        
        // 测试每种技能类型的创建
        for (AttackSkillJ.SkillType type : AttackSkillJ.SkillType.values()) {
            AttackSkillJ skill = new AttackSkillJ(0, null, random, type);
            assertNotNull(skill);
            assertEquals(type, skill.getSkillType());
        }
    }
    
    @Test
    public void testSkillTypeGetter() {
        Random random = new Random(42);
        
        // 测试使用带类型构造函数
        AttackSkillJ metalSkill = new AttackSkillJ(0, null, random, AttackSkillJ.SkillType.METAL);
        assertEquals(AttackSkillJ.SkillType.METAL, metalSkill.getSkillType());
        
        AttackSkillJ fireSkill = new AttackSkillJ(1, null, random, AttackSkillJ.SkillType.FIRE);
        assertEquals(AttackSkillJ.SkillType.FIRE, fireSkill.getSkillType());
    }
    
    @Test
    public void testDefaultSkillType() {
        Random random = new Random(42);
        
        // 使用不带类型的构造函数，应有默认类型
        AttackSkillJ skill = new AttackSkillJ(0, null, random);
        assertNotNull(skill.getSkillType());
    }
    
    @Test
    public void testSkillWithTypeInitializePosition() {
        Random random = new Random(42);
        
        // 测试带类型技能的位置初始化
        AttackSkillJ waterSkill = new AttackSkillJ(0, null, random, AttackSkillJ.SkillType.WATER);
        waterSkill.initializePosition(new Vector2(200, 300), random);
        
        TransformComponent transform = waterSkill.getComponent(TransformComponent.class);
        assertNotNull(transform);
        assertEquals(200, transform.getPosition().x, 0.001f);
        assertEquals(300, transform.getPosition().y, 0.001f);
    }
    
    @Test
    public void testSkillWithTypeUpdate() {
        Random random = new Random(42);
        
        // 测试各种技能类型的更新不抛出异常
        for (AttackSkillJ.SkillType type : AttackSkillJ.SkillType.values()) {
            AttackSkillJ skill = new AttackSkillJ(0, null, random, type);
            skill.initializePosition(new Vector2(100, 100), random);
            skill.update(0.016f);  // 不应抛出异常
        }
    }
}
