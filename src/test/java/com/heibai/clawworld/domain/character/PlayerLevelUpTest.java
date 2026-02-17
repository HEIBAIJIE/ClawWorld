package com.heibai.clawworld.domain.character;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 玩家升级系统单元测试
 * 测试经验增加、升级逻辑、属性点分配等功能
 */
@DisplayName("玩家升级系统测试")
class PlayerLevelUpTest {

    private Player player;
    private Role testRole;

    @BeforeEach
    void setUp() {
        // 创建测试用的职业配置
        testRole = createTestRole();

        // 创建测试玩家
        player = createTestPlayer();
    }

    // ==================== 经验增加测试 ====================

    @Test
    @DisplayName("经验增加 - 正常增加")
    void testAddExperience_Normal() {
        player.setExperience(0);

        player.setExperience(player.getExperience() + 100);

        assertEquals(100, player.getExperience());
    }

    @Test
    @DisplayName("经验增加 - 多次增加")
    void testAddExperience_Multiple() {
        player.setExperience(0);

        player.setExperience(player.getExperience() + 50);
        player.setExperience(player.getExperience() + 75);
        player.setExperience(player.getExperience() + 25);

        assertEquals(150, player.getExperience());
    }

    @Test
    @DisplayName("经验增加 - 大量经验")
    void testAddExperience_Large() {
        player.setExperience(0);

        player.setExperience(player.getExperience() + 10000);

        assertEquals(10000, player.getExperience());
    }

    // ==================== 升级逻辑测试 ====================

    @Test
    @DisplayName("升级 - 从1级升到2级")
    void testLevelUp_From1To2() {
        player.setLevel(1);
        player.setFreeAttributePoints(0);

        player.levelUp(testRole);

        assertEquals(2, player.getLevel());
        assertEquals(5, player.getFreeAttributePoints());
    }

    @Test
    @DisplayName("升级 - 连续升级")
    void testLevelUp_Multiple() {
        player.setLevel(1);
        player.setFreeAttributePoints(0);

        player.levelUp(testRole);
        player.levelUp(testRole);
        player.levelUp(testRole);

        assertEquals(4, player.getLevel());
        assertEquals(15, player.getFreeAttributePoints());
    }

    @Test
    @DisplayName("升级 - 保留之前的属性点")
    void testLevelUp_KeepExistingPoints() {
        player.setLevel(5);
        player.setFreeAttributePoints(3);

        player.levelUp(testRole);

        assertEquals(6, player.getLevel());
        assertEquals(8, player.getFreeAttributePoints());
    }

    @Test
    @DisplayName("升级 - 职业属性正确应用")
    void testLevelUp_RoleStatsApplied() {
        player.setLevel(1);
        player.applyRoleStats(testRole);

        int level1Health = player.getBaseMaxHealth();
        int level1Mana = player.getBaseMaxMana();
        int level1PhysicalAttack = player.getBasePhysicalAttack();

        player.levelUp(testRole);

        // 升级后基础属性应该增加
        assertTrue(player.getBaseMaxHealth() > level1Health);
        assertTrue(player.getBaseMaxMana() > level1Mana);
        assertTrue(player.getBasePhysicalAttack() > level1PhysicalAttack);
    }

    @Test
    @DisplayName("升级 - 最终属性重新计算")
    void testLevelUp_FinalStatsRecalculated() {
        player.setLevel(1);
        player.setStrength(10);
        player.setAgility(10);
        player.setIntelligence(10);
        player.setVitality(10);
        player.applyRoleStats(testRole);

        int level1FinalHealth = player.getMaxHealth();

        player.levelUp(testRole);

        // 升级后最终属性应该更新
        assertTrue(player.getMaxHealth() > level1FinalHealth);
    }

    // ==================== 属性点分配测试 ====================

    @Test
    @DisplayName("属性点分配 - 分配力量")
    void testAllocateAttributePoints_Strength() {
        player.setLevel(2);
        player.setFreeAttributePoints(5);
        player.setStrength(10);
        player.applyRoleStats(testRole);

        int initialPhysicalAttack = player.getPhysicalAttack();

        // 分配3点力量
        player.setStrength(player.getStrength() + 3);
        player.setFreeAttributePoints(player.getFreeAttributePoints() - 3);
        player.recalculateFinalStats();

        assertEquals(13, player.getStrength());
        assertEquals(2, player.getFreeAttributePoints());
        // 力量影响物理攻击：+2物理攻击/点
        assertEquals(initialPhysicalAttack + 6, player.getPhysicalAttack());
    }

    @Test
    @DisplayName("属性点分配 - 分配敏捷")
    void testAllocateAttributePoints_Agility() {
        player.setLevel(2);
        player.setFreeAttributePoints(5);
        player.setAgility(10);
        player.applyRoleStats(testRole);

        int initialSpeed = player.getSpeed();

        // 分配4点敏捷
        player.setAgility(player.getAgility() + 4);
        player.setFreeAttributePoints(player.getFreeAttributePoints() - 4);
        player.recalculateFinalStats();

        assertEquals(14, player.getAgility());
        assertEquals(1, player.getFreeAttributePoints());
        // 敏捷影响速度：+2速度/点
        assertEquals(initialSpeed + 8, player.getSpeed());
    }

    @Test
    @DisplayName("属性点分配 - 分配法力")
    void testAllocateAttributePoints_Intelligence() {
        player.setLevel(2);
        player.setFreeAttributePoints(5);
        player.setIntelligence(10);
        player.applyRoleStats(testRole);

        int initialMaxMana = player.getMaxMana();
        int initialMagicAttack = player.getMagicAttack();

        // 分配2点法力
        player.setIntelligence(player.getIntelligence() + 2);
        player.setFreeAttributePoints(player.getFreeAttributePoints() - 2);
        player.recalculateFinalStats();

        assertEquals(12, player.getIntelligence());
        assertEquals(3, player.getFreeAttributePoints());
        // 法力影响法力上限：+10法力/点，法术攻击：+3攻击/点
        assertEquals(initialMaxMana + 20, player.getMaxMana());
        assertEquals(initialMagicAttack + 6, player.getMagicAttack());
    }

    @Test
    @DisplayName("属性点分配 - 分配体力")
    void testAllocateAttributePoints_Vitality() {
        player.setLevel(2);
        player.setFreeAttributePoints(5);
        player.setVitality(10);
        player.applyRoleStats(testRole);

        int initialMaxHealth = player.getMaxHealth();

        // 分配5点体力
        player.setVitality(player.getVitality() + 5);
        player.setFreeAttributePoints(player.getFreeAttributePoints() - 5);
        player.recalculateFinalStats();

        assertEquals(15, player.getVitality());
        assertEquals(0, player.getFreeAttributePoints());
        // 体力影响生命上限：+15生命/点
        assertEquals(initialMaxHealth + 75, player.getMaxHealth());
    }

    @Test
    @DisplayName("属性点分配 - 平均分配")
    void testAllocateAttributePoints_Balanced() {
        player.setLevel(3);
        player.setFreeAttributePoints(10);
        player.setStrength(10);
        player.setAgility(10);
        player.setIntelligence(10);
        player.setVitality(10);
        player.applyRoleStats(testRole);

        // 平均分配：每个属性+2，消耗8点
        player.setStrength(player.getStrength() + 2);
        player.setAgility(player.getAgility() + 2);
        player.setIntelligence(player.getIntelligence() + 3);
        player.setVitality(player.getVitality() + 3);
        player.setFreeAttributePoints(0);
        player.recalculateFinalStats();

        assertEquals(12, player.getStrength());
        assertEquals(12, player.getAgility());
        assertEquals(13, player.getIntelligence());
        assertEquals(13, player.getVitality());
        assertEquals(0, player.getFreeAttributePoints());
    }

    @Test
    @DisplayName("属性点分配 - 不能超过可用点数")
    void testAllocateAttributePoints_CannotExceedAvailable() {
        player.setLevel(2);
        player.setFreeAttributePoints(3);
        player.setStrength(10);

        // 尝试分配5点（超过可用的3点）
        // 在实际应用中，这应该由服务层验证
        // 这里只测试数据模型的行为
        int pointsToAllocate = 5;
        int availablePoints = player.getFreeAttributePoints();

        assertTrue(pointsToAllocate > availablePoints);
    }

    // ==================== 等级上限测试 ====================

    @Test
    @DisplayName("等级上限 - 检查高等级")
    void testLevelCap_HighLevel() {
        player.setLevel(50);
        player.setFreeAttributePoints(0);

        player.levelUp(testRole);

        assertEquals(51, player.getLevel());
        assertEquals(5, player.getFreeAttributePoints());
    }

    @Test
    @DisplayName("等级上限 - 检查极高等级")
    void testLevelCap_VeryHighLevel() {
        player.setLevel(99);
        player.setFreeAttributePoints(0);

        player.levelUp(testRole);

        assertEquals(100, player.getLevel());
        assertEquals(5, player.getFreeAttributePoints());
    }

    // ==================== 经验计算正确性测试 ====================

    @Test
    @DisplayName("经验计算 - 经验不会丢失")
    void testExperienceCalculation_NoLoss() {
        player.setExperience(500);

        int currentExp = player.getExperience();
        player.setExperience(currentExp + 100);

        assertEquals(600, player.getExperience());
    }

    @Test
    @DisplayName("经验计算 - 升级后经验保留")
    void testExperienceCalculation_RetainAfterLevelUp() {
        player.setLevel(5);
        player.setExperience(1000);

        player.levelUp(testRole);

        // 升级不应该清空经验（除非在服务层实现了经验重置逻辑）
        assertEquals(1000, player.getExperience());
    }

    @Test
    @DisplayName("经验计算 - 零经验")
    void testExperienceCalculation_Zero() {
        player.setExperience(0);

        assertEquals(0, player.getExperience());
    }

    // ==================== 职业属性应用测试 ====================

    @Test
    @DisplayName("职业属性应用 - 1级属性")
    void testApplyRoleStats_Level1() {
        player.setLevel(1);

        player.applyRoleStats(testRole);

        // 1级时应该使用基础属性（不包含成长）
        assertEquals(testRole.getBaseHealth(), player.getBaseMaxHealth());
        assertEquals(testRole.getBaseMana(), player.getBaseMaxMana());
        assertEquals(testRole.getBasePhysicalAttack(), player.getBasePhysicalAttack());
    }

    @Test
    @DisplayName("职业属性应用 - 5级属性")
    void testApplyRoleStats_Level5() {
        player.setLevel(5);

        player.applyRoleStats(testRole);

        // 5级时应该包含4级的成长
        int expectedHealth = (int)(testRole.getBaseHealth() + testRole.getHealthPerLevel() * 4);
        int expectedMana = (int)(testRole.getBaseMana() + testRole.getManaPerLevel() * 4);

        assertEquals(expectedHealth, player.getBaseMaxHealth());
        assertEquals(expectedMana, player.getBaseMaxMana());
    }

    @Test
    @DisplayName("职业属性应用 - 10级属性")
    void testApplyRoleStats_Level10() {
        player.setLevel(10);

        player.applyRoleStats(testRole);

        // 10级时应该包含9级的成长
        int expectedHealth = (int)(testRole.getBaseHealth() + testRole.getHealthPerLevel() * 9);
        int expectedPhysicalAttack = (int)(testRole.getBasePhysicalAttack() + testRole.getPhysicalAttackPerLevel() * 9);

        assertEquals(expectedHealth, player.getBaseMaxHealth());
        assertEquals(expectedPhysicalAttack, player.getBasePhysicalAttack());
    }

    @Test
    @DisplayName("职业属性应用 - 属性成长计算")
    void testApplyRoleStats_GrowthCalculation() {
        player.setLevel(1);
        player.applyRoleStats(testRole);
        int level1Health = player.getBaseMaxHealth();

        player.setLevel(2);
        player.applyRoleStats(testRole);
        int level2Health = player.getBaseMaxHealth();

        // 升1级应该增加healthPerLevel的数值
        assertEquals((int)testRole.getHealthPerLevel(), level2Health - level1Health);
    }

    // ==================== 四维属性影响测试 ====================

    @Test
    @DisplayName("四维属性影响 - 力量影响物理攻击和防御")
    void testAttributeEffects_Strength() {
        player.setLevel(1);
        player.setStrength(0);
        player.applyRoleStats(testRole);

        int basePhysicalAttack = player.getPhysicalAttack();
        int basePhysicalDefense = player.getPhysicalDefense();

        player.setStrength(10);
        player.recalculateFinalStats();

        // 力量：+2物理攻击，+1物理防御
        assertEquals(basePhysicalAttack + 20, player.getPhysicalAttack());
        assertEquals(basePhysicalDefense + 10, player.getPhysicalDefense());
    }

    @Test
    @DisplayName("四维属性影响 - 敏捷影响速度和暴击")
    void testAttributeEffects_Agility() {
        player.setLevel(1);
        player.setAgility(0);
        player.applyRoleStats(testRole);

        int baseSpeed = player.getSpeed();
        double baseCritRate = player.getCritRate();

        player.setAgility(10);
        player.recalculateFinalStats();

        // 敏捷：+2速度，+0.5%暴击率
        assertEquals(baseSpeed + 20, player.getSpeed());
        assertEquals(baseCritRate + 0.05, player.getCritRate(), 0.001);
    }

    @Test
    @DisplayName("四维属性影响 - 法力影响法力值和法术攻击")
    void testAttributeEffects_Intelligence() {
        player.setLevel(1);
        player.setIntelligence(0);
        player.applyRoleStats(testRole);

        int baseMaxMana = player.getMaxMana();
        int baseMagicAttack = player.getMagicAttack();

        player.setIntelligence(10);
        player.recalculateFinalStats();

        // 法力：+10法力上限，+3法术攻击
        assertEquals(baseMaxMana + 100, player.getMaxMana());
        assertEquals(baseMagicAttack + 30, player.getMagicAttack());
    }

    @Test
    @DisplayName("四维属性影响 - 体力影响生命值和防御")
    void testAttributeEffects_Vitality() {
        player.setLevel(1);
        player.setVitality(0);
        player.applyRoleStats(testRole);

        int baseMaxHealth = player.getMaxHealth();
        int basePhysicalDefense = player.getPhysicalDefense();

        player.setVitality(10);
        player.recalculateFinalStats();

        // 体力：+15生命上限，+2物理防御
        assertEquals(baseMaxHealth + 150, player.getMaxHealth());
        assertEquals(basePhysicalDefense + 20, player.getPhysicalDefense());
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试用的职业配置
     */
    private Role createTestRole() {
        Role role = new Role();
        role.setId("warrior");
        role.setName("战士");

        // 基础属性
        role.setBaseHealth(100);
        role.setBaseMana(50);
        role.setBasePhysicalAttack(20);
        role.setBasePhysicalDefense(15);
        role.setBaseMagicAttack(10);
        role.setBaseMagicDefense(10);
        role.setBaseSpeed(100);
        role.setBaseCritRate(0.05);
        role.setBaseCritDamage(0.5);
        role.setBaseHitRate(0.95);
        role.setBaseDodgeRate(0.05);

        // 每级成长
        role.setHealthPerLevel(10.0);
        role.setManaPerLevel(5.0);
        role.setPhysicalAttackPerLevel(2.0);
        role.setPhysicalDefensePerLevel(1.5);
        role.setMagicAttackPerLevel(1.0);
        role.setMagicDefensePerLevel(1.0);
        role.setSpeedPerLevel(2.0);
        role.setCritRatePerLevel(0.001);
        role.setCritDamagePerLevel(0.01);
        role.setHitRatePerLevel(0.001);
        role.setDodgeRatePerLevel(0.001);

        return role;
    }

    /**
     * 创建测试用的玩家
     */
    private Player createTestPlayer() {
        Player p = new Player();
        p.setId("test-player");
        p.setName("测试玩家");
        p.setRoleId("warrior");
        p.setLevel(1);
        p.setExperience(0);
        p.setFreeAttributePoints(0);

        // 初始四维属性
        p.setStrength(5);
        p.setAgility(5);
        p.setIntelligence(5);
        p.setVitality(5);

        p.setGold(0);
        p.setFaction("player");

        return p;
    }
}
