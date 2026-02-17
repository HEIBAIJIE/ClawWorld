package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.CombatService;
import com.heibai.clawworld.application.service.PlayerSessionService;
import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.combat.CombatCharacter;
import com.heibai.clawworld.domain.combat.CombatInstance;
import com.heibai.clawworld.domain.service.CombatEngine;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.map.MapConfig;
import com.heibai.clawworld.infrastructure.persistence.mapper.CombatMapper;
import com.heibai.clawworld.infrastructure.persistence.mapper.ConfigMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("战斗服务测试")
class CombatServiceImplTest {

    @Mock
    private CombatEngine combatEngine;

    @Mock
    private ConfigDataManager configDataManager;

    @Mock
    private ConfigMapper configMapper;

    @Mock
    private CombatMapper combatMapper;

    @Mock
    private PlayerSessionService playerSessionService;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private CombatServiceImpl combatService;

    private Player attacker;
    private Player target;
    private MapConfig mapConfig;

    @BeforeEach
    void setUp() {
        attacker = new Player();
        attacker.setId("player1");
        attacker.setName("攻击者");
        attacker.setMapId("test_map");
        attacker.setX(5);
        attacker.setY(5);
        attacker.setFaction("FACTION_A");

        target = new Player();
        target.setId("player2");
        target.setName("目标");
        target.setMapId("test_map");
        target.setX(5);
        target.setY(5);
        target.setFaction("FACTION_B");

        mapConfig = new MapConfig();
        mapConfig.setId("test_map");
        mapConfig.setName("测试地图");
        mapConfig.setSafe(false);
    }

    @Test
    @DisplayName("发起战斗 - 成功")
    void testInitiateCombat_Success() {
        when(playerSessionService.getPlayerState("player1")).thenReturn(attacker);
        when(playerSessionService.getPlayerState("player2")).thenReturn(target);
        when(configDataManager.getMap("test_map")).thenReturn(mapConfig);
        when(playerRepository.findAll()).thenReturn(new ArrayList<>());
        when(combatEngine.createCombat("test_map")).thenReturn("combat123");

        // Mock combatMapper to return a CombatCharacter for any Player
        CombatCharacter attackerCombatChar = new CombatCharacter();
        attackerCombatChar.setCharacterId("player1");
        attackerCombatChar.setFactionId("FACTION_A");

        CombatCharacter targetCombatChar = new CombatCharacter();
        targetCombatChar.setCharacterId("player2");
        targetCombatChar.setFactionId("FACTION_B");

        when(combatMapper.toCombatCharacter(attacker)).thenReturn(attackerCombatChar);
        when(combatMapper.toCombatCharacter(target)).thenReturn(targetCombatChar);

        CombatService.CombatResult result = combatService.initiateCombat("player1", "player2");

        assertTrue(result.isSuccess());
        assertEquals("combat123", result.getCombatId());
        verify(combatEngine).createCombat("test_map");
        verify(combatEngine, times(2)).addPartyToCombat(eq("combat123"), anyString(), anyList());
    }

    @Test
    @DisplayName("发起战斗 - 攻击者不存在")
    void testInitiateCombat_AttackerNotFound() {
        when(playerSessionService.getPlayerState("player1")).thenReturn(null);

        CombatService.CombatResult result = combatService.initiateCombat("player1", "player2");

        assertFalse(result.isSuccess());
        assertEquals("攻击者不存在", result.getMessage());
    }

    @Test
    @DisplayName("发起战斗 - 目标不存在")
    void testInitiateCombat_TargetNotFound() {
        when(playerSessionService.getPlayerState("player1")).thenReturn(attacker);
        when(playerSessionService.getPlayerState("player2")).thenReturn(null);

        CombatService.CombatResult result = combatService.initiateCombat("player1", "player2");

        assertFalse(result.isSuccess());
        assertEquals("目标不存在", result.getMessage());
    }

    @Test
    @DisplayName("发起战斗 - 不在同一地图")
    void testInitiateCombat_DifferentMap() {
        target.setMapId("other_map");
        when(playerSessionService.getPlayerState("player1")).thenReturn(attacker);
        when(playerSessionService.getPlayerState("player2")).thenReturn(target);

        CombatService.CombatResult result = combatService.initiateCombat("player1", "player2");

        assertFalse(result.isSuccess());
        assertEquals("目标不在同一地图", result.getMessage());
    }

    @Test
    @DisplayName("发起战斗 - 安全地图不允许战斗")
    void testInitiateCombat_SafeMap() {
        mapConfig.setSafe(true);
        when(playerSessionService.getPlayerState("player1")).thenReturn(attacker);
        when(playerSessionService.getPlayerState("player2")).thenReturn(target);
        when(configDataManager.getMap("test_map")).thenReturn(mapConfig);

        CombatService.CombatResult result = combatService.initiateCombat("player1", "player2");

        assertFalse(result.isSuccess());
        assertEquals("当前地图不允许战斗", result.getMessage());
    }

    @Test
    @DisplayName("发起战斗 - 同阵营不能攻击")
    void testInitiateCombat_SameFaction() {
        target.setFaction("FACTION_A");
        when(playerSessionService.getPlayerState("player1")).thenReturn(attacker);
        when(playerSessionService.getPlayerState("player2")).thenReturn(target);
        when(configDataManager.getMap("test_map")).thenReturn(mapConfig);

        CombatService.CombatResult result = combatService.initiateCombat("player1", "player2");

        assertFalse(result.isSuccess());
        assertEquals("不能攻击同阵营角色", result.getMessage());
    }

    @Test
    @DisplayName("释放技能 - 成功")
    void testCastSkill_Success() {
        // 创建战斗实例
        CombatInstance combat = new CombatInstance("combat123", "test_map");

        // 添加玩家角色
        com.heibai.clawworld.domain.combat.CombatCharacter playerChar = new com.heibai.clawworld.domain.combat.CombatCharacter();
        playerChar.setCharacterId("player1");
        playerChar.setName("攻击者");
        playerChar.setFactionId("FACTION_A");
        playerChar.setMaxHealth(100);
        playerChar.setCurrentHealth(100);

        // 添加敌人角色
        com.heibai.clawworld.domain.combat.CombatCharacter enemyChar = new com.heibai.clawworld.domain.combat.CombatCharacter();
        enemyChar.setCharacterId("enemy1");
        enemyChar.setName("敌人");
        enemyChar.setFactionId("FACTION_B");
        enemyChar.setMaxHealth(50);
        enemyChar.setCurrentHealth(50);

        com.heibai.clawworld.domain.combat.CombatParty playerParty = new com.heibai.clawworld.domain.combat.CombatParty("FACTION_A");
        playerParty.addCharacter(playerChar);
        combat.addParty("FACTION_A", playerParty);

        com.heibai.clawworld.domain.combat.CombatParty enemyParty = new com.heibai.clawworld.domain.combat.CombatParty("FACTION_B");
        enemyParty.addCharacter(enemyChar);
        combat.addParty("FACTION_B", enemyParty);

        when(combatEngine.getCombat("combat123")).thenReturn(Optional.of(combat));

        CombatEngine.CombatActionResult actionResult = new CombatEngine.CombatActionResult();
        actionResult.setSuccess(true);
        actionResult.setMessage("技能释放成功");
        actionResult.setBattleLog(new ArrayList<>());
        actionResult.setCombatEnded(false);

        // 由于会自动选择目标，所以targetId不再是null
        when(combatEngine.executeSkillWithWait(eq("combat123"), eq("player1"), anyString(), anyString()))
            .thenReturn(actionResult);

        CombatService.ActionResult result = combatService.castSkill("combat123", "player1", "火球术");

        assertTrue(result.isSuccess());
        assertFalse(result.isCombatEnded());
    }

    @Test
    @DisplayName("获取战斗状态")
    void testGetCombatState() {
        CombatInstance combat = new CombatInstance("combat123", "test_map");
        when(combatEngine.getCombat("combat123")).thenReturn(Optional.of(combat));

        var result = combatService.getCombatState("combat123");

        assertNotNull(result);
    }
}
