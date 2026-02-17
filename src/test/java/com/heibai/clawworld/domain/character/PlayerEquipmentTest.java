package com.heibai.clawworld.domain.character;

import com.heibai.clawworld.domain.item.Equipment;
import com.heibai.clawworld.domain.item.Rarity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Player装备系统单元测试
 * 测试装备穿戴、卸下、属性加成计算等功能
 */
@DisplayName("玩家装备系统测试")
class PlayerEquipmentTest {

    private Player player;
    private Role role;

    @BeforeEach
    void setUp() {
        // 初始化玩家
        player = new Player();
        player.setId("test-player");
        player.setRoleId("warrior");
        player.setLevel(1);
        player.setStrength(10);
        player.setAgility(8);
        player.setIntelligence(5);
        player.setVitality(12);
        player.setEquipment(new HashMap<>());
        player.setInventory(new ArrayList<>());

        // 初始化职业
        role = new Role();
        role.setId("warrior");
        role.setName("战士");
        role.setBaseHealth(100);
        role.setBaseMana(50);
        role.setBasePhysicalAttack(20);
        role.setBasePhysicalDefense(15);
        role.setBaseMagicAttack(5);
        role.setBaseMagicDefense(10);
        role.setBaseSpeed(10);
        role.setBaseCritRate(0.05);
        role.setBaseCritDamage(1.5);
        role.setBaseHitRate(0.95);
        role.setBaseDodgeRate(0.05);
        role.setHealthPerLevel(10);
        role.setManaPerLevel(5);
        role.setPhysicalAttackPerLevel(2);
        role.setPhysicalDefensePerLevel(1.5);
        role.setMagicAttackPerLevel(0.5);
        role.setMagicDefensePerLevel(1);
        role.setSpeedPerLevel(1);
        role.setCritRatePerLevel(0.001);
        role.setCritDamagePerLevel(0.01);
        role.setHitRatePerLevel(0.001);
        role.setDodgeRatePerLevel(0.001);

        // 应用职业属性
        player.applyRoleStats(role);
    }

    // ==================== 装备穿戴测试 ====================

    @Test
    @DisplayName("装备穿戴 - 空槽位穿戴成功")
    void testEquipItem_EmptySlot_Success() {
        // 创建一把武器
        Equipment weapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);

        // 记录穿戴前的属性
        int beforePhysicalAttack = player.getPhysicalAttack();

        // 穿戴装备
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.recalculateFinalStats();

        // 验证装备已穿戴
        Equipment equipped = player.getEquipment().get(Equipment.EquipmentSlot.RIGHT_HAND);
        assertNotNull(equipped);
        assertEquals("铁剑", equipped.getName());
        assertEquals(1L, equipped.getInstanceNumber());

        // 验证属性增加
        int afterPhysicalAttack = player.getPhysicalAttack();
        assertEquals(beforePhysicalAttack + 10, afterPhysicalAttack);
    }

    @Test
    @DisplayName("装备穿戴 - 替换已有装备")
    void testEquipItem_ReplaceExisting() {
        // 先穿戴一把武器
        Equipment oldWeapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, oldWeapon);
        player.recalculateFinalStats();

        int afterFirstEquip = player.getPhysicalAttack();

        // 穿戴更好的武器
        Equipment newWeapon = createWeapon("钢剑", 2L, 20, 0, 0, 0);
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, newWeapon);
        player.recalculateFinalStats();

        // 验证新装备已穿戴
        Equipment equipped = player.getEquipment().get(Equipment.EquipmentSlot.RIGHT_HAND);
        assertEquals("钢剑", equipped.getName());
        assertEquals(2L, equipped.getInstanceNumber());

        // 验证属性正确更新（增加了10点物理攻击）
        assertEquals(afterFirstEquip + 10, player.getPhysicalAttack());
    }

    @Test
    @DisplayName("装备穿戴 - 多个槽位同时装备")
    void testEquipItem_MultipleSlots() {
        // 创建多件装备
        Equipment weapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        Equipment helmet = createArmor("铁盔", 2L, Equipment.EquipmentSlot.HEAD, 0, 5, 0, 0);
        Equipment chest = createArmor("铁甲", 3L, Equipment.EquipmentSlot.CHEST, 0, 10, 0, 0);

        int beforePhysicalAttack = player.getPhysicalAttack();
        int beforePhysicalDefense = player.getPhysicalDefense();

        // 穿戴所有装备
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.getEquipment().put(Equipment.EquipmentSlot.HEAD, helmet);
        player.getEquipment().put(Equipment.EquipmentSlot.CHEST, chest);
        player.recalculateFinalStats();

        // 验证所有装备都已穿戴
        assertEquals(3, player.getEquipment().size());
        assertNotNull(player.getEquipment().get(Equipment.EquipmentSlot.RIGHT_HAND));
        assertNotNull(player.getEquipment().get(Equipment.EquipmentSlot.HEAD));
        assertNotNull(player.getEquipment().get(Equipment.EquipmentSlot.CHEST));

        // 验证属性累加正确
        assertEquals(beforePhysicalAttack + 10, player.getPhysicalAttack());
        assertEquals(beforePhysicalDefense + 15, player.getPhysicalDefense());
    }

    // ==================== 装备卸下测试 ====================

    @Test
    @DisplayName("装备卸下 - 成功卸下")
    void testUnequipItem_Success() {
        // 先穿戴装备
        Equipment weapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.recalculateFinalStats();

        int afterEquip = player.getPhysicalAttack();

        // 卸下装备
        player.getEquipment().remove(Equipment.EquipmentSlot.RIGHT_HAND);
        player.recalculateFinalStats();

        // 验证装备已卸下
        assertNull(player.getEquipment().get(Equipment.EquipmentSlot.RIGHT_HAND));

        // 验证属性恢复
        assertEquals(afterEquip - 10, player.getPhysicalAttack());
    }

    @Test
    @DisplayName("装备卸下 - 卸下空槽位")
    void testUnequipItem_EmptySlot() {
        // 记录初始属性
        int beforePhysicalAttack = player.getPhysicalAttack();

        // 尝试卸下空槽位（不应该报错）
        player.getEquipment().remove(Equipment.EquipmentSlot.RIGHT_HAND);
        player.recalculateFinalStats();

        // 验证属性没有变化
        assertEquals(beforePhysicalAttack, player.getPhysicalAttack());
    }

    @Test
    @DisplayName("装备卸下 - 卸下所有装备")
    void testUnequipItem_RemoveAll() {
        // 穿戴多件装备
        Equipment weapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        Equipment helmet = createArmor("铁盔", 2L, Equipment.EquipmentSlot.HEAD, 0, 5, 0, 0);
        Equipment chest = createArmor("铁甲", 3L, Equipment.EquipmentSlot.CHEST, 0, 10, 0, 0);

        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.getEquipment().put(Equipment.EquipmentSlot.HEAD, helmet);
        player.getEquipment().put(Equipment.EquipmentSlot.CHEST, chest);
        player.recalculateFinalStats();

        int beforePhysicalAttack = player.getPhysicalAttack();
        int beforePhysicalDefense = player.getPhysicalDefense();

        // 卸下所有装备
        player.getEquipment().clear();
        player.recalculateFinalStats();

        // 验证所有装备已卸下
        assertTrue(player.getEquipment().isEmpty());

        // 验证属性恢复
        assertEquals(beforePhysicalAttack - 10, player.getPhysicalAttack());
        assertEquals(beforePhysicalDefense - 15, player.getPhysicalDefense());
    }

    // ==================== 装备属性加成测试 ====================

    @Test
    @DisplayName("装备属性加成 - 四维属性加成")
    void testEquipmentBonus_FourDimensionalAttributes() {
        // 创建提供四维属性的装备
        Equipment ring = createAccessory("力量戒指", 1L, 5, 0, 0, 0);

        int beforePhysicalAttack = player.getPhysicalAttack();
        int beforePhysicalDefense = player.getPhysicalDefense();

        // 穿戴装备
        player.getEquipment().put(Equipment.EquipmentSlot.ACCESSORY1, ring);
        player.recalculateFinalStats();

        // 验证四维属性影响战斗属性
        // 力量：+2物理攻击，+1物理防御，+0.1%命中
        assertEquals(beforePhysicalAttack + 5 * 2, player.getPhysicalAttack());
        assertEquals(beforePhysicalDefense + 5, player.getPhysicalDefense());
    }

    @Test
    @DisplayName("装备属性加成 - 直接战斗属性加成")
    void testEquipmentBonus_DirectCombatAttributes() {
        // 创建提供直接战斗属性的装备
        Equipment weapon = createWeapon("铁剑", 1L, 15, 0, 0, 0);
        weapon.setCritRate(0.05);
        weapon.setCritDamage(0.1);

        int beforePhysicalAttack = player.getPhysicalAttack();
        double beforeCritRate = player.getCritRate();
        double beforeCritDamage = player.getCritDamage();

        // 穿戴装备
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.recalculateFinalStats();

        // 验证直接属性加成
        assertEquals(beforePhysicalAttack + 15, player.getPhysicalAttack());
        assertEquals(beforeCritRate + 0.05, player.getCritRate(), 0.001);
        assertEquals(beforeCritDamage + 0.1, player.getCritDamage(), 0.001);
    }

    @Test
    @DisplayName("装备属性加成 - 四维和直接属性同时加成")
    void testEquipmentBonus_BothTypes() {
        // 创建同时提供四维和直接属性的装备
        Equipment weapon = createWeapon("精良铁剑", 1L, 10, 0, 0, 0);
        weapon.setStrength(3);
        weapon.setAgility(2);

        int beforePhysicalAttack = player.getPhysicalAttack();
        int beforeSpeed = player.getSpeed();

        // 穿戴装备
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.recalculateFinalStats();

        // 验证属性加成
        // 直接物理攻击+10，力量3提供+6物理攻击，敏捷2提供+4速度
        assertEquals(beforePhysicalAttack + 10 + 3 * 2, player.getPhysicalAttack());
        assertEquals(beforeSpeed + 2 * 2, player.getSpeed());
    }

    @Test
    @DisplayName("装备属性加成 - 多件装备属性累加")
    void testEquipmentBonus_MultipleEquipments() {
        // 创建多件装备
        Equipment weapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        weapon.setStrength(2);

        Equipment helmet = createArmor("铁盔", 2L, Equipment.EquipmentSlot.HEAD, 0, 5, 0, 0);
        helmet.setVitality(3);

        Equipment chest = createArmor("铁甲", 3L, Equipment.EquipmentSlot.CHEST, 0, 8, 0, 0);
        chest.setVitality(5);

        int beforePhysicalAttack = player.getPhysicalAttack();
        int beforePhysicalDefense = player.getPhysicalDefense();
        int beforeMaxHealth = player.getMaxHealth();

        // 穿戴所有装备
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.getEquipment().put(Equipment.EquipmentSlot.HEAD, helmet);
        player.getEquipment().put(Equipment.EquipmentSlot.CHEST, chest);
        player.recalculateFinalStats();

        // 验证属性累加
        // 物理攻击：直接+10，力量2提供+4
        assertEquals(beforePhysicalAttack + 10 + 2 * 2, player.getPhysicalAttack());
        // 物理防御：直接+13，力量2提供+2，体力8提供+16
        assertEquals(beforePhysicalDefense + 13 + 2 + 8 * 2, player.getPhysicalDefense());
        // 生命值：体力8提供+120
        assertEquals(beforeMaxHealth + 8 * 15, player.getMaxHealth());
    }

    // ==================== recalculateFinalStats方法测试 ====================

    @Test
    @DisplayName("recalculateFinalStats - 无装备时正确计算")
    void testRecalculateFinalStats_NoEquipment() {
        // 清空装备
        player.getEquipment().clear();
        player.recalculateFinalStats();

        // 验证属性 = 职业基础 + 玩家四维影响
        // 力量10：+20物理攻击，+10物理防御
        // 敏捷8：+16速度
        // 法力5：+50法力上限，+15法术攻击，+5法术防御
        // 体力12：+180生命上限，+24物理防御，+6法术防御
        assertEquals(100 + 12 * 15, player.getMaxHealth());
        assertEquals(50 + 5 * 10, player.getMaxMana());
        assertEquals(20 + 10 * 2, player.getPhysicalAttack());
        assertEquals(15 + 10 + 12 * 2, player.getPhysicalDefense());
        assertEquals(5 + 5 * 3, player.getMagicAttack());
        assertEquals(10 + 5 + (int)(12 * 0.5), player.getMagicDefense());
        assertEquals(10 + 8 * 2, player.getSpeed());
    }

    @Test
    @DisplayName("recalculateFinalStats - 有装备时正确计算")
    void testRecalculateFinalStats_WithEquipment() {
        // 穿戴装备
        Equipment weapon = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        weapon.setStrength(5);

        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.recalculateFinalStats();

        // 验证属性 = 职业基础 + (玩家四维+装备四维)影响 + 装备直接属性
        // 总力量15：+30物理攻击，+15物理防御
        // 敏捷8：+16速度
        // 法力5：+50法力上限，+15法术攻击，+5法术防御
        // 体力12：+180生命上限，+24物理防御，+6法术防御
        // 装备直接：+10物理攻击
        assertEquals(20 + 15 * 2 + 10, player.getPhysicalAttack());
        assertEquals(15 + 15 + 12 * 2, player.getPhysicalDefense());
    }

    @Test
    @DisplayName("recalculateFinalStats - 装备变化后重新计算")
    void testRecalculateFinalStats_AfterEquipmentChange() {
        // 第一次穿戴
        Equipment weapon1 = createWeapon("铁剑", 1L, 10, 0, 0, 0);
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon1);
        player.recalculateFinalStats();
        int firstAttack = player.getPhysicalAttack();

        // 更换装备
        Equipment weapon2 = createWeapon("钢剑", 2L, 20, 0, 0, 0);
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon2);
        player.recalculateFinalStats();
        int secondAttack = player.getPhysicalAttack();

        // 验证属性正确更新
        assertEquals(firstAttack + 10, secondAttack);

        // 卸下装备
        player.getEquipment().remove(Equipment.EquipmentSlot.RIGHT_HAND);
        player.recalculateFinalStats();
        int thirdAttack = player.getPhysicalAttack();

        // 验证属性恢复
        assertEquals(firstAttack - 10, thirdAttack);
    }

    // ==================== 边界条件测试 ====================

    @Test
    @DisplayName("边界条件 - 装备Map为null")
    void testBoundary_NullEquipmentMap() {
        player.setEquipment(null);

        // 不应该抛出异常
        assertDoesNotThrow(() -> player.recalculateFinalStats());
    }

    @Test
    @DisplayName("边界条件 - 装备Map为空")
    void testBoundary_EmptyEquipmentMap() {
        player.getEquipment().clear();

        int beforePhysicalAttack = player.getPhysicalAttack();

        // 重新计算不应该改变属性
        player.recalculateFinalStats();

        assertEquals(beforePhysicalAttack, player.getPhysicalAttack());
    }

    @Test
    @DisplayName("边界条件 - 装备属性为0")
    void testBoundary_ZeroEquipmentStats() {
        Equipment weapon = createWeapon("木剑", 1L, 0, 0, 0, 0);

        int beforePhysicalAttack = player.getPhysicalAttack();

        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, weapon);
        player.recalculateFinalStats();

        // 属性不应该改变
        assertEquals(beforePhysicalAttack, player.getPhysicalAttack());
    }

    @Test
    @DisplayName("边界条件 - 装备槽位中有null值")
    void testBoundary_NullEquipmentInSlot() {
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND, null);

        // 不应该抛出异常
        assertDoesNotThrow(() -> player.recalculateFinalStats());
    }

    @Test
    @DisplayName("边界条件 - 所有装备槽位都装备")
    void testBoundary_AllSlotsEquipped() {
        // 装备所有槽位
        player.getEquipment().put(Equipment.EquipmentSlot.HEAD,
            createArmor("头盔", 1L, Equipment.EquipmentSlot.HEAD, 0, 5, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.CHEST,
            createArmor("胸甲", 2L, Equipment.EquipmentSlot.CHEST, 0, 10, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.LEGS,
            createArmor("护腿", 3L, Equipment.EquipmentSlot.LEGS, 0, 8, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.FEET,
            createArmor("靴子", 4L, Equipment.EquipmentSlot.FEET, 0, 5, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.LEFT_HAND,
            createWeapon("盾牌", 5L, 0, 15, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.RIGHT_HAND,
            createWeapon("剑", 6L, 20, 0, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.ACCESSORY1,
            createAccessory("戒指1", 7L, 3, 0, 0, 0));
        player.getEquipment().put(Equipment.EquipmentSlot.ACCESSORY2,
            createAccessory("戒指2", 8L, 0, 3, 0, 0));

        // 不应该抛出异常
        assertDoesNotThrow(() -> player.recalculateFinalStats());

        // 验证所有装备都生效
        assertEquals(8, player.getEquipment().size());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建武器
     */
    private Equipment createWeapon(String name, Long instanceNumber,
                                   int physicalAttack, int physicalDefense,
                                   int magicAttack, int magicDefense) {
        Equipment equipment = new Equipment();
        equipment.setId("weapon-" + instanceNumber);
        equipment.setName(name);
        equipment.setInstanceNumber(instanceNumber);
        equipment.setSlot(Equipment.EquipmentSlot.RIGHT_HAND);
        equipment.setRarity(Rarity.COMMON);
        equipment.setPhysicalAttack(physicalAttack);
        equipment.setPhysicalDefense(physicalDefense);
        equipment.setMagicAttack(magicAttack);
        equipment.setMagicDefense(magicDefense);
        return equipment;
    }

    /**
     * 创建护甲
     */
    private Equipment createArmor(String name, Long instanceNumber,
                                  Equipment.EquipmentSlot slot,
                                  int physicalAttack, int physicalDefense,
                                  int magicAttack, int magicDefense) {
        Equipment equipment = new Equipment();
        equipment.setId("armor-" + instanceNumber);
        equipment.setName(name);
        equipment.setInstanceNumber(instanceNumber);
        equipment.setSlot(slot);
        equipment.setRarity(Rarity.COMMON);
        equipment.setPhysicalAttack(physicalAttack);
        equipment.setPhysicalDefense(physicalDefense);
        equipment.setMagicAttack(magicAttack);
        equipment.setMagicDefense(magicDefense);
        return equipment;
    }

    /**
     * 创建饰品
     */
    private Equipment createAccessory(String name, Long instanceNumber,
                                      int strength, int agility,
                                      int intelligence, int vitality) {
        Equipment equipment = new Equipment();
        equipment.setId("accessory-" + instanceNumber);
        equipment.setName(name);
        equipment.setInstanceNumber(instanceNumber);
        equipment.setSlot(Equipment.EquipmentSlot.ACCESSORY1);
        equipment.setRarity(Rarity.COMMON);
        equipment.setStrength(strength);
        equipment.setAgility(agility);
        equipment.setIntelligence(intelligence);
        equipment.setVitality(vitality);
        return equipment;
    }
}
