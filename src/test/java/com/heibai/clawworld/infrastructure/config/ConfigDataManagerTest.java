package com.heibai.clawworld.infrastructure.config;

import com.heibai.clawworld.infrastructure.config.data.character.*;
import com.heibai.clawworld.infrastructure.config.data.item.*;
import com.heibai.clawworld.infrastructure.config.data.map.*;
import com.heibai.clawworld.infrastructure.config.data.skill.*;
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
        log.info("传送点数量: {}", configDataManager.getAllWaypoints().size());

        assertFalse(configDataManager.getAllItems().isEmpty(), "应该加载了物品数据");
        assertFalse(configDataManager.getAllEquipment().isEmpty(), "应该加载了装备数据");
        assertFalse(configDataManager.getAllSkills().isEmpty(), "应该加载了技能数据");
        assertFalse(configDataManager.getAllEnemies().isEmpty(), "应该加载了敌人数据");
        assertFalse(configDataManager.getAllNpcs().isEmpty(), "应该加载了NPC数据");
        assertFalse(configDataManager.getAllMaps().isEmpty(), "应该加载了地图数据");
        assertFalse(configDataManager.getAllRoles().isEmpty(), "应该加载了职业数据");
        assertFalse(configDataManager.getAllWaypoints().isEmpty(), "应该加载了传送点数据");

        log.info("\n=== 关联配置数据 ===");
        // 动态获取第一个可用的ID进行测试，避免硬编码依赖CSV具体内容
        if (!configDataManager.getAllEnemies().isEmpty()) {
            String firstEnemyId = configDataManager.getAllEnemies().stream().findFirst().get().getId();
            log.info("敌人掉落配置 ({}): {} 条", firstEnemyId, configDataManager.getEnemyLoot(firstEnemyId).size());
        }

        if (!configDataManager.getAllMaps().isEmpty()) {
            String firstMapId = configDataManager.getAllMaps().stream().findFirst().get().getId();
            log.info("地图地形配置 ({}): {} 条", firstMapId, configDataManager.getMapTerrain(firstMapId).size());
            log.info("地图实体配置 ({}): {} 条", firstMapId, configDataManager.getMapEntities(firstMapId).size());
        }

        if (!configDataManager.getAllNpcs().isEmpty()) {
            String firstNpcId = configDataManager.getAllNpcs().stream().findFirst().get().getId();
            log.info("NPC商店物品 ({}): {} 条", firstNpcId, configDataManager.getNpcShopItems(firstNpcId).size());
        }

        if (!configDataManager.getAllRoles().isEmpty()) {
            String firstRoleId = configDataManager.getAllRoles().stream().findFirst().get().getId();
            log.info("职业技能映射 ({}): {} 条", firstRoleId, configDataManager.getRoleSkills(firstRoleId).size());
        }

        log.info("=== 配置加载完成 ===");
    }
}
