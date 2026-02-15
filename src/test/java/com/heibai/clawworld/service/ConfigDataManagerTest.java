package com.heibai.clawworld.service;

import com.heibai.clawworld.model.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConfigDataManagerTest {

    private static final Logger log = LoggerFactory.getLogger(ConfigDataManagerTest.class);

    @Autowired
    private ConfigDataManager configDataManager;

    @Test
    void testConfigDataLoaded() {
        log.info("=== 配置数据加载验证 ===");
        log.info("物品数量: {}", configDataManager.getAllItems().size());
        log.info("装备数量: {}", configDataManager.getAllEquipment().size());
        log.info("技能数量: {}", configDataManager.getAllSkills().size());
        log.info("敌人数量: {}", configDataManager.getAllEnemies().size());
        log.info("NPC数量: {}", configDataManager.getAllNpcs().size());
        log.info("地图数量: {}", configDataManager.getAllMaps().size());
        log.info("职业数量: {}", configDataManager.getAllRoles().size());

        assertFalse(configDataManager.getAllItems().isEmpty(), "应该加载了物品数据");
        assertFalse(configDataManager.getAllEquipment().isEmpty(), "应该加载了装备数据");
        assertFalse(configDataManager.getAllSkills().isEmpty(), "应该加载了技能数据");
        assertFalse(configDataManager.getAllEnemies().isEmpty(), "应该加载了敌人数据");
        assertFalse(configDataManager.getAllNpcs().isEmpty(), "应该加载了NPC数据");
        assertFalse(configDataManager.getAllMaps().isEmpty(), "应该加载了地图数据");
        assertFalse(configDataManager.getAllRoles().isEmpty(), "应该加载了职业数据");

        log.info("\n=== 示例数据 ===");
        configDataManager.getAllItems().stream().limit(3).forEach(item ->
            log.info("物品: {} - {} (基础价格: {})", item.getName(), item.getDescription(), item.getBasePrice())
        );

        configDataManager.getAllSkills().stream().limit(3).forEach(skill ->
            log.info("技能: {} - {} (伤害倍率: {})", skill.getName(), skill.getDescription(), skill.getDamageMultiplier())
        );

        configDataManager.getAllEnemies().stream().limit(3).forEach(enemy ->
            log.info("敌人: {} Lv.{} - {} (生命: {}, 攻击: {}, 命中: {}, 闪避: {})",
                enemy.getName(), enemy.getLevel(), enemy.getTier(), enemy.getHealth(),
                enemy.getPhysicalAttack(), enemy.getHitRate(), enemy.getDodgeRate())
        );

        configDataManager.getAllRoles().forEach(role ->
            log.info("职业: {} - 基础属性 HP:{} MP:{} STR:{} AGI:{} INT:{} VIT:{} (技能数: {})",
                role.getName(), role.getBaseHealth(), role.getBaseMana(),
                role.getBaseStrength(), role.getBaseAgility(), role.getBaseIntelligence(),
                role.getBaseVitality(), role.getSkillLearns().size())
        );

        log.info("=== 配置加载完成 ===");
    }
}
