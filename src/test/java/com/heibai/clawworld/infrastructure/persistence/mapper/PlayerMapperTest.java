package com.heibai.clawworld.infrastructure.persistence.mapper;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PlayerMapper单元测试
 */
class PlayerMapperTest {

    private PlayerMapper playerMapper;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        playerMapper = new PlayerMapper();

        // 创建测试玩家（仅包含游戏数据）
        testPlayer = new Player();
        testPlayer.setId("test_id");
        testPlayer.setRoleId("WARRIOR");
        testPlayer.setLevel(5);
        testPlayer.setExperience(500);
        testPlayer.setStrength(15);
        testPlayer.setAgility(10);
        testPlayer.setIntelligence(5);
        testPlayer.setVitality(12);
        testPlayer.setFreeAttributePoints(3);
        testPlayer.setMaxHealth(150);
        testPlayer.setCurrentHealth(120);
        testPlayer.setMaxMana(80);
        testPlayer.setCurrentMana(60);
        testPlayer.setPhysicalAttack(35);
        testPlayer.setPhysicalDefense(25);
        testPlayer.setMagicAttack(10);
        testPlayer.setMagicDefense(15);
        testPlayer.setSpeed(105);
        testPlayer.setCritRate(0.08);
        testPlayer.setCritDamage(0.6);
        testPlayer.setHitRate(1.02);
        testPlayer.setDodgeRate(0.08);
        testPlayer.setGold(500);
        testPlayer.setX(10);
        testPlayer.setY(15);
        testPlayer.setPartyId("party_1");
        testPlayer.setPartyLeader(true);
        testPlayer.setInCombat(false);

        // 设置装备和背包
        testPlayer.setEquipment(new HashMap<>());
        testPlayer.setInventory(new ArrayList<>());
    }

    @Test
    void testToEntity() {
        PlayerEntity entity = playerMapper.toEntity(testPlayer);

        assertNotNull(entity);
        assertEquals("test_id", entity.getId());
        assertEquals("WARRIOR", entity.getRoleId());
        assertEquals(5, entity.getLevel());
        assertEquals(500, entity.getExperience());
        assertEquals(15, entity.getStrength());
        assertEquals(10, entity.getAgility());
        assertEquals(5, entity.getIntelligence());
        assertEquals(12, entity.getVitality());
        assertEquals(3, entity.getFreeAttributePoints());
        assertEquals(150, entity.getMaxHealth());
        assertEquals(120, entity.getCurrentHealth());
        assertEquals(80, entity.getMaxMana());
        assertEquals(60, entity.getCurrentMana());
        assertEquals(35, entity.getPhysicalAttack());
        assertEquals(25, entity.getPhysicalDefense());
        assertEquals(10, entity.getMagicAttack());
        assertEquals(15, entity.getMagicDefense());
        assertEquals(105, entity.getSpeed());
        assertEquals(0.08, entity.getCritRate(), 0.001);
        assertEquals(0.6, entity.getCritDamage(), 0.001);
        assertEquals(1.02, entity.getHitRate(), 0.001);
        assertEquals(0.08, entity.getDodgeRate(), 0.001);
        assertEquals(500, entity.getGold());
        assertEquals(10, entity.getX());
        assertEquals(15, entity.getY());
        assertEquals("party_1", entity.getPartyId());
        assertTrue(entity.isPartyLeader());
        assertFalse(entity.isInCombat());
    }

    @Test
    void testToDomain() {
        PlayerEntity entity = new PlayerEntity();
        entity.setId("test_id");
        entity.setRoleId("WARRIOR");
        entity.setLevel(5);
        entity.setExperience(500);
        entity.setStrength(15);
        entity.setAgility(10);
        entity.setIntelligence(5);
        entity.setVitality(12);
        entity.setFreeAttributePoints(3);
        entity.setMaxHealth(150);
        entity.setCurrentHealth(120);
        entity.setMaxMana(80);
        entity.setCurrentMana(60);
        entity.setPhysicalAttack(35);
        entity.setPhysicalDefense(25);
        entity.setMagicAttack(10);
        entity.setMagicDefense(15);
        entity.setSpeed(105);
        entity.setCritRate(0.08);
        entity.setCritDamage(0.6);
        entity.setHitRate(1.02);
        entity.setDodgeRate(0.08);
        entity.setGold(500);
        entity.setX(10);
        entity.setY(15);
        entity.setPartyId("party_1");
        entity.setPartyLeader(true);
        entity.setInCombat(false);

        Player player = playerMapper.toDomain(entity);

        assertNotNull(player);
        assertEquals("test_id", player.getId());
        assertEquals("WARRIOR", player.getRoleId());
        assertEquals(5, player.getLevel());
        assertEquals(500, player.getExperience());
        assertEquals(15, player.getStrength());
        assertEquals(10, player.getAgility());
        assertEquals(5, player.getIntelligence());
        assertEquals(12, player.getVitality());
        assertEquals(3, player.getFreeAttributePoints());
        assertEquals(150, player.getMaxHealth());
        assertEquals(120, player.getCurrentHealth());
        assertEquals(80, player.getMaxMana());
        assertEquals(60, player.getCurrentMana());
        assertEquals(35, player.getPhysicalAttack());
        assertEquals(25, player.getPhysicalDefense());
        assertEquals(10, player.getMagicAttack());
        assertEquals(15, player.getMagicDefense());
        assertEquals(105, player.getSpeed());
        assertEquals(0.08, player.getCritRate(), 0.001);
        assertEquals(0.6, player.getCritDamage(), 0.001);
        assertEquals(1.02, player.getHitRate(), 0.001);
        assertEquals(0.08, player.getDodgeRate(), 0.001);
        assertEquals(500, player.getGold());
        assertEquals(10, player.getX());
        assertEquals(15, player.getY());
        assertEquals("party_1", player.getPartyId());
        assertTrue(player.isPartyLeader());
        assertFalse(player.isInCombat());
    }

    @Test
    void testToEntityWithNullPlayer() {
        PlayerEntity entity = playerMapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    void testToDomainWithNullEntity() {
        Player player = playerMapper.toDomain(null);
        assertNull(player);
    }

    @Test
    void testRoundTripConversion() {
        // 领域对象 -> 实体 -> 领域对象
        PlayerEntity entity = playerMapper.toEntity(testPlayer);
        Player converted = playerMapper.toDomain(entity);

        assertNotNull(converted);
        assertEquals(testPlayer.getId(), converted.getId());
        assertEquals(testPlayer.getRoleId(), converted.getRoleId());
        assertEquals(testPlayer.getLevel(), converted.getLevel());
        assertEquals(testPlayer.getGold(), converted.getGold());
    }
}
