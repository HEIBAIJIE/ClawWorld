package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.infrastructure.config.data.map.MapConfig;
import com.heibai.clawworld.domain.map.MapEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PlayerMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.application.service.MapEntityService;
import com.heibai.clawworld.application.service.PlayerSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MapEntityServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private ConfigDataManager configDataManager;

    @Mock
    private PlayerSessionService playerSessionService;

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

        // Act
        MapEntityService.InteractionResult result = mapEntityService.interact("player1", "enemy1", "attack");

        // Assert
        assertTrue(result.isSuccess());
        assertTrue(result.isWindowChanged());
        assertEquals("COMBAT", result.getNewWindowType());
    }

    @Test
    void testInteract_Shop() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));

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

        // Act
        MapEntityService.InteractionResult result = mapEntityService.interact("player1", "npc1", "talk");

        // Assert
        assertTrue(result.isSuccess());
        assertFalse(result.isWindowChanged());
    }

    @Test
    void testGetNearbyInteractableEntities_ReturnsEntities() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(testPlayer));
        when(playerRepository.findAll()).thenReturn(Collections.singletonList(testPlayer));

        // Act
        List<MapEntity> result = mapEntityService.getNearbyInteractableEntities("player1");

        // Assert
        assertNotNull(result);
    }

    @Test
    void testGetMapEntities_ReturnsEntities() {
        // Arrange
        when(playerRepository.findAll()).thenReturn(Collections.singletonList(testPlayer));

        // Act
        List<MapEntity> result = mapEntityService.getMapEntities("map1");

        // Assert
        assertNotNull(result);
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
