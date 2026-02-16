package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.ShopService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ShopServiceImpl 单元测试
 */
class ShopServiceImplTest {

    private ShopServiceImpl shopService;

    @BeforeEach
    void setUp() {
        shopService = new ShopServiceImpl();
    }

    @Test
    void testBuyItem() {
        ShopService.OperationResult result = shopService.buyItem(
                "player1",
                "shop1",
                "生命药剂",
                5
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("购买"));
        assertTrue(result.getMessage().contains("生命药剂"));
    }

    @Test
    void testSellItem() {
        ShopService.OperationResult result = shopService.sellItem(
                "player1",
                "shop1",
                "铁剑",
                1
        );

        assertTrue(result.isSuccess());
        assertNotNull(result.getMessage());
        assertTrue(result.getMessage().contains("出售"));
        assertTrue(result.getMessage().contains("铁剑"));
    }

    @Test
    void testGetShopInfo() {
        ShopService.ShopInfo info = shopService.getShopInfo("shop1");

        assertNotNull(info);
        assertEquals("shop1", info.getShopId());
        assertEquals("shop1", info.getNpcName());
        assertEquals(10000, info.getGold());
    }
}
