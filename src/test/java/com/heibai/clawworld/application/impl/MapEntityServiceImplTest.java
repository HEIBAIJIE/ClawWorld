package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.*;
import com.heibai.clawworld.infrastructure.config.data.map.MapConfig;
import com.heibai.clawworld.domain.map.MapEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.repository.EnemyInstanceRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.NpcShopInstanceRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapEntityServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private ConfigDataManager configDataManager;

    @Mock
    private PlayerSessionService playerSessionService;

    @Mock
    private EnemyInstanceRepository enemyInstanceRepository;

    @Mock
    private NpcShopInstanceRepository npcShopInstanceRepository;

    // 新增的委托服务
    @Mock
    private PathfindingService pathfindingService;

    @Mock
    private MapEntityQueryService mapEntityQueryService;

    @Mock
    private TeleportService teleportService;

    @Mock
    private DialogueService dialogueService;

    @Mock
    private RestService restService;

    @Mock
    private PartyService partyService;

    @Mock
    private TradeService tradeService;

    @Mock
    private CombatService combatService;

    @Mock
    private CharacterInfoService characterInfoService;

    @InjectMocks
    private MapEntityServiceImpl mapEntityService;

    private PlayerEntity testPlayer;
    private MapConfig testMap;

    @BeforeEach
    void setUp() {
        testPlayer = new PlayerEntity();
        testPlayer.setId("player1");
        testPlayer.setCurrentMapId("map1");
        testPlayer.setX(5);
        testPlayer.setY(5);
        testPlayer.setInCombat(false);

        testMap = new MapConfig();
        testMap.setId("map1");
        testMap.setName("测试地图");
        testMap.setWidth(10);
        testMap.setHeight(10);
    }

    @Test
    void testMovePlayer_Success() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(configDataManager.getMap("map1")).thenReturn(testMap);
        when(pathfindingService.isPositionPassable("map1", 6, 6)).thenReturn(true);
        when(pathfindingService.findPath("map1", 5, 5, 6, 6)).thenReturn(List.of(new int[]{6, 6}));
        when(playerRepository.save(any(PlayerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        MapEntityService.MoveResult result = mapEntityService.movePlayer("player1", 6, 6);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals(6, result.getCurrentX());
        assertEquals(6, result.getCurrentY());
        verify(playerRepository).save(any(PlayerEntity.class));
    }

    @Test
    void testMovePlayer_InCombat() {
        // Arrange
        testPlayer.setInCombat(true);
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));

        // Act
        MapEntityService.MoveResult result = mapEntityService.movePlayer("player1", 6, 6);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("战斗中无法移动", result.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    void testMovePlayer_OutOfBounds() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(configDataManager.getMap("map1")).thenReturn(testMap);

        // Act
        MapEntityService.MoveResult result = mapEntityService.movePlayer("player1", 20, 20);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("目标位置超出地图范围", result.getMessage());
    }

    @Test
    void testInteract_Attack() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(enemyInstanceRepository.findByMapId("map1")).thenReturn(Collections.emptyList());
        when(playerRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        MapEntityService.InteractionResult result = mapEntityService.interact("player1", "enemy1", "attack");

        // Assert
        // 由于没有找到敌人或玩家，应该返回错误
        assertFalse(result.isSuccess());
    }

    @Test
    void testInteract_Shop() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));

        // Mock NPC shop instance
        var npcShopInstance = new com.heibai.clawworld.infrastructure.persistence.entity.NpcShopInstanceEntity();
        npcShopInstance.setNpcId("npc1");
        npcShopInstance.setMapId("map1");
        when(npcShopInstanceRepository.findByMapId("map1")).thenReturn(java.util.List.of(npcShopInstance));

        // Mock NPC config
        var npcConfig = new com.heibai.clawworld.infrastructure.config.data.character.NpcConfig();
        npcConfig.setId("npc1");
        npcConfig.setName("npc1");
        npcConfig.setHasShop(true);
        when(configDataManager.getNpc("npc1")).thenReturn(npcConfig);

        // Act
        MapEntityService.InteractionResult result = mapEntityService.interact("player1", "npc1", "shop");

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.isWindowChanged());
        assertEquals("SHOP", result.getNewWindowType());
    }

    @Test
    void testInteract_Talk() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(dialogueService.talk("player1", "npc1")).thenReturn(
                DialogueService.DialogueResult.success("与 npc1 对话", List.of("你好"))
        );

        // Act
        MapEntityService.InteractionResult result = mapEntityService.interact("player1", "npc1", "talk");

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(result.isWindowChanged());
    }

    @Test
    void testGetNearbyInteractableEntities_ReturnsEntities() {
        // Arrange
        when(mapEntityQueryService.getNearbyInteractableEntities("player1")).thenReturn(new ArrayList<>());

        // Act
        List<MapEntity> result = mapEntityService.getNearbyInteractableEntities("player1");

        // Assert
        assertNotNull(result);
        verify(mapEntityQueryService).getNearbyInteractableEntities("player1");
    }

    @Test
    void testGetMapEntities_ReturnsEntities() {
        // Arrange
        when(mapEntityQueryService.getMapEntities("map1")).thenReturn(new ArrayList<>());

        // Act
        List<MapEntity> result = mapEntityService.getMapEntities("map1");

        // Assert
        assertNotNull(result);
        verify(mapEntityQueryService).getMapEntities("map1");
    }

    @Test
    void testInspectCharacter_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.empty());

        // Act
        MapEntityService.EntityInfo result = mapEntityService.inspectCharacter("player1", "target");

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("玩家不存在", result.getMessage());
    }
}
