package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.ShopService;
import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.item.Item;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.NpcConfig;
import com.heibai.clawworld.infrastructure.config.data.character.NpcShopItemConfig;
import com.heibai.clawworld.infrastructure.config.data.item.ItemConfig;
import com.heibai.clawworld.infrastructure.persistence.entity.NpcShopInstanceEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PlayerMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.NpcShopInstanceRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 商店服务单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商店服务测试")
class ShopServiceImplTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerMapper playerMapper;

    @Mock
    private NpcShopInstanceRepository npcShopInstanceRepository;

    @Mock
    private ConfigDataManager configDataManager;

    @InjectMocks
    private ShopServiceImpl shopService;

    private PlayerEntity playerEntity;
    private Player player;
    private NpcShopInstanceEntity shop;
    private NpcConfig npcConfig;
    private ItemConfig itemConfig;

    @BeforeEach
    void setUp() {
        // 创建测试玩家
        playerEntity = new PlayerEntity();
        playerEntity.setId("player1");
        playerEntity.setGold(1000);

        player = new Player();
        player.setId("player1");
        player.setGold(1000);
        player.setInventory(new ArrayList<>());

        // 创建测试商店
        shop = new NpcShopInstanceEntity();
        shop.setNpcId("merchant_john");
        shop.setCurrentGold(10000);
        shop.setItems(new ArrayList<>());

        // 添加商店物品
        NpcShopInstanceEntity.ShopItemData shopItem = new NpcShopInstanceEntity.ShopItemData();
        shopItem.setItemId("health_potion");
        shopItem.setMaxQuantity(100);
        shopItem.setCurrentQuantity(50);
        shop.getItems().add(shopItem);

        // 创建NPC配置
        npcConfig = new NpcConfig();
        npcConfig.setId("merchant_john");
        npcConfig.setName("商人约翰");
        npcConfig.setHasShop(true);
        npcConfig.setPriceMultiplier(0.5); // 设置价格倍率

        // 创建物品配置
        itemConfig = new ItemConfig();
        itemConfig.setId("health_potion");
        itemConfig.setName("生命药剂");
        itemConfig.setBasePrice(50);
        itemConfig.setMaxStack(99);
    }

    @Test
    @DisplayName("购买物品 - 成功")
    void testBuyItem_Success() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toDomain(playerEntity)).thenReturn(player);
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.of(shop));
        when(configDataManager.getNpc("merchant_john")).thenReturn(npcConfig);
        when(configDataManager.getItem("生命药剂")).thenReturn(itemConfig);
        when(playerMapper.toEntity(any(Player.class))).thenReturn(playerEntity);
        when(playerRepository.save(any(PlayerEntity.class))).thenReturn(playerEntity);
        when(npcShopInstanceRepository.save(any(NpcShopInstanceEntity.class))).thenReturn(shop);

        // Act
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "生命药剂", 5);

        // Assert
        assertTrue(result.isSuccess(), "购买成功: " + result.getMessage());
        assertTrue(result.getMessage().contains("购买成功") || result.getMessage().contains("成功"));
        verify(playerRepository).save(any(PlayerEntity.class));
        verify(npcShopInstanceRepository).save(any(NpcShopInstanceEntity.class));
    }

    @Test
    @DisplayName("购买物品 - 玩家不存在")
    void testBuyItem_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.empty());

        // Act
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "生命药剂", 5);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("玩家不存在", result.getMessage());
        verify(playerRepository, never()).save(any());
    }

    @Test
    @DisplayName("购买物品 - 商店不存在")
    void testBuyItem_ShopNotFound() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toDomain(playerEntity)).thenReturn(player);
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.empty());

        // Act
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "生命药剂", 5);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("商店不存在", result.getMessage());
    }

    @Test
    @DisplayName("购买物品 - 物品不存在")
    void testBuyItem_ItemNotFound() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toDomain(playerEntity)).thenReturn(player);
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.of(shop));
        when(configDataManager.getNpc("merchant_john")).thenReturn(npcConfig);
        when(configDataManager.getItem("不存在的物品")).thenReturn(null);

        // Act
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "不存在的物品", 5);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("物品不存在"));
    }

    @Test
    @DisplayName("购买物品 - 库存不足")
    void testBuyItem_InsufficientStock() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toDomain(playerEntity)).thenReturn(player);
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.of(shop));
        when(configDataManager.getNpc("merchant_john")).thenReturn(npcConfig);
        when(configDataManager.getItem("生命药剂")).thenReturn(itemConfig);

        // Act - 尝试购买超过库存的数量
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "生命药剂", 100);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("库存不足"));
    }

    @Test
    @DisplayName("购买物品 - 金钱不足")
    void testBuyItem_InsufficientGold() {
        // Arrange
        player.setGold(100); // 只有100金币
        when(playerRepository.findById("player1")).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toDomain(playerEntity)).thenReturn(player);
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.of(shop));
        when(configDataManager.getNpc("merchant_john")).thenReturn(npcConfig);
        when(configDataManager.getItem("生命药剂")).thenReturn(itemConfig);

        // Act - 尝试购买5个,需要250金币
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "生命药剂", 5);

        // Assert
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("金钱不足"));
    }

    @Test
    @DisplayName("购买物品 - 背包已满")
    void testBuyItem_InventoryFull() {
        // Arrange
        // 填满背包
        for (int i = 0; i < 50; i++) {
            Item item = new Item();
            item.setId("item_" + i);
            player.getInventory().add(Player.InventorySlot.forItem(item, 1));
        }

        when(playerRepository.findById("player1")).thenReturn(Optional.of(playerEntity));
        when(playerMapper.toDomain(playerEntity)).thenReturn(player);
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.of(shop));
        when(configDataManager.getNpc("merchant_john")).thenReturn(npcConfig);
        when(configDataManager.getItem("生命药剂")).thenReturn(itemConfig);

        // Act
        ShopService.OperationResult result = shopService.buyItem("player1", "merchant_john", "生命药剂", 5);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("背包已满", result.getMessage());
    }

    @Test
    @DisplayName("出售物品 - 玩家不存在")
    void testSellItem_PlayerNotFound() {
        // Arrange
        when(playerRepository.findById("player1")).thenReturn(Optional.empty());

        // Act
        ShopService.OperationResult result = shopService.sellItem("player1", "merchant_john", "生命药剂", 5);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("玩家不存在", result.getMessage());
    }

    @Test
    @DisplayName("获取商店信息 - 商店不存在")
    void testGetShopInfo_ShopNotFound() {
        // Arrange
        when(configDataManager.getNpc("merchant_john")).thenReturn(null);

        // Act
        ShopService.ShopInfo result = shopService.getShopInfo("merchant_john");

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("获取商店信息 - 成功")
    void testGetShopInfo_Success() {
        // Arrange
        when(npcShopInstanceRepository.findByNpcId("merchant_john")).thenReturn(Optional.of(shop));
        when(configDataManager.getNpc("merchant_john")).thenReturn(npcConfig);

        // Mock shop item configs
        NpcShopItemConfig shopItemConfig = new NpcShopItemConfig();
        shopItemConfig.setNpcId("merchant_john");
        shopItemConfig.setItemId("health_potion");
        shopItemConfig.setQuantity(100);
        when(configDataManager.getNpcShopItems("merchant_john"))
            .thenReturn(java.util.Collections.singletonList(shopItemConfig));

        when(configDataManager.getItem("health_potion")).thenReturn(itemConfig);

        // Act
        ShopService.ShopInfo result = shopService.getShopInfo("merchant_john");

        // Assert
        assertNotNull(result);
        assertEquals("merchant_john", result.getShopId());
        assertEquals("商人约翰", result.getNpcName());
        assertEquals(10000, result.getGold());
        assertNotNull(result.getItems());
        assertFalse(result.getItems().isEmpty());
        assertEquals(1, result.getItems().size());
        assertEquals("health_potion", result.getItems().get(0).getItemId());
        assertEquals("生命药剂", result.getItems().get(0).getItemName());
    }
}
