package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.infrastructure.persistence.entity.AccountEntity;
import com.heibai.clawworld.infrastructure.persistence.repository.AccountRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("状态服务测试")
class StateServiceImplTest {

    @Mock
    private com.heibai.clawworld.application.service.PlayerSessionService playerSessionService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private com.heibai.clawworld.application.service.TradeService tradeService;

    @Mock
    private com.heibai.clawworld.application.service.ShopService shopService;

    @Mock
    private com.heibai.clawworld.domain.service.CombatEngine combatEngine;

    @Mock
    private com.heibai.clawworld.application.service.ChatService chatService;

    @Mock
    private com.heibai.clawworld.application.service.MapEntityService mapEntityService;

    @Mock
    private com.heibai.clawworld.infrastructure.config.ConfigDataManager configDataManager;

    @Mock
    private com.heibai.clawworld.application.service.PartyService partyService;

    @Mock
    private com.heibai.clawworld.infrastructure.persistence.repository.TradeRepository tradeRepository;

    @InjectMocks
    private StateServiceImpl stateService;

    private Player testPlayer;
    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        testPlayer = new Player();
        testPlayer.setId("player1");
        testPlayer.setName("测试玩家");
        testPlayer.setLevel(5);
        testPlayer.setCurrentHealth(100);
        testPlayer.setMaxHealth(150);
        testPlayer.setCurrentMana(50);
        testPlayer.setMaxMana(80);

        testAccount = new AccountEntity();
        testAccount.setPlayerId("player1");
        testAccount.setNickname("测试玩家");
    }

    @Test
    @DisplayName("生成地图窗口状态")
    void testGenerateMapState() {
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));
        lenient().when(chatService.getChatHistory("player1")).thenReturn(new ArrayList<>());
        lenient().when(mapEntityService.getMapEntities(anyString())).thenReturn(new ArrayList<>());

        String state = stateService.generateMapState("player1", "移动成功");

        assertNotNull(state);
        assertTrue(state.length() > 0);
        verify(accountRepository).findByPlayerId("player1");
    }

    @Test
    @DisplayName("生成战斗窗口状态")
    void testGenerateCombatState() {
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));

        String state = stateService.generateCombatState("player1", "combat123", "技能释放成功");

        assertNotNull(state);
    }

    @Test
    @DisplayName("生成交易窗口状态")
    void testGenerateTradeState() {
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));

        String state = stateService.generateTradeState("player1", "trade123", "添加物品成功");

        assertNotNull(state);
    }

    @Test
    @DisplayName("生成商店窗口状态")
    void testGenerateShopState() {
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));

        String state = stateService.generateShopState("player1", "shop123", "购买成功");

        assertNotNull(state);
    }

    @Test
    @DisplayName("更新最后状态时间戳")
    void testUpdateLastStateTimestamp() {
        when(accountRepository.findByPlayerId("player1")).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(AccountEntity.class))).thenReturn(testAccount);

        stateService.updateLastStateTimestamp("player1");

        Long timestamp = stateService.getLastStateTimestamp("player1");
        assertNotNull(timestamp);
        assertTrue(timestamp > 0);
    }

    @Test
    @DisplayName("获取不存在玩家的时间戳")
    void testGetLastStateTimestamp_NotFound() {
        when(accountRepository.findByPlayerId("nonexistent")).thenReturn(Optional.empty());

        Long timestamp = stateService.getLastStateTimestamp("nonexistent");

        assertNull(timestamp);
    }
}
