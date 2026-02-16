package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.PlayerEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 玩家Repository单元测试
 * 使用内嵌MongoDB进行测试，不依赖CSV配置数据
 */
@DataMongoTest
class PlayerRepositoryTest {

    @Autowired
    private PlayerRepository playerRepository;

    private PlayerEntity testPlayer;

    @BeforeEach
    void setUp() {
        // 创建测试数据（仅包含游戏数据，不包含账号信息）
        testPlayer = new PlayerEntity();
        testPlayer.setRoleId("WARRIOR");
        testPlayer.setLevel(1);
        testPlayer.setExperience(0);
        testPlayer.setStrength(10);
        testPlayer.setAgility(5);
        testPlayer.setIntelligence(3);
        testPlayer.setVitality(8);
        testPlayer.setFreeAttributePoints(0);
        testPlayer.setMaxHealth(100);
        testPlayer.setCurrentHealth(100);
        testPlayer.setMaxMana(50);
        testPlayer.setCurrentMana(50);
        testPlayer.setPhysicalAttack(25);
        testPlayer.setPhysicalDefense(15);
        testPlayer.setMagicAttack(5);
        testPlayer.setMagicDefense(8);
        testPlayer.setSpeed(100);
        testPlayer.setCritRate(0.05);
        testPlayer.setCritDamage(0.5);
        testPlayer.setHitRate(1.0);
        testPlayer.setDodgeRate(0.05);
        testPlayer.setGold(100);
        testPlayer.setEquipment(new HashMap<>());
        testPlayer.setInventory(new ArrayList<>());
        testPlayer.setCurrentMapId("starter_village");
        testPlayer.setX(5);
        testPlayer.setY(5);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        playerRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        // 保存玩家
        PlayerEntity saved = playerRepository.save(testPlayer);
        assertNotNull(saved.getId());

        // 根据ID查找
        Optional<PlayerEntity> found = playerRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("WARRIOR", found.get().getRoleId());
        assertEquals(1, found.get().getLevel());
    }

    @Test
    void testUpdatePlayer() {
        PlayerEntity saved = playerRepository.save(testPlayer);

        // 更新玩家数据
        saved.setLevel(2);
        saved.setExperience(100);
        saved.setGold(200);
        playerRepository.save(saved);

        // 验证更新
        Optional<PlayerEntity> updated = playerRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals(2, updated.get().getLevel());
        assertEquals(100, updated.get().getExperience());
        assertEquals(200, updated.get().getGold());
    }

    @Test
    void testDeletePlayer() {
        PlayerEntity saved = playerRepository.save(testPlayer);
        assertNotNull(saved.getId());

        playerRepository.deleteById(saved.getId());

        Optional<PlayerEntity> deleted = playerRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }
}
