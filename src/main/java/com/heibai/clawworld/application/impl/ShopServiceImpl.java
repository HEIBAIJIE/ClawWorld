package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 商店服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopServiceImpl implements ShopService {

    @Override
    public OperationResult buyItem(String playerId, String shopId, String itemName, int quantity) {
        // TODO: 实现完整的购买逻辑
        // 1. 验证玩家是否有足够的金钱
        // 2. 验证商店是否有足够的库存
        // 3. 扣除玩家金钱
        // 4. 减少商店库存
        // 5. 添加物品到玩家背包
        log.info("玩家 {} 从商店 {} 购买 {} x{}", playerId, shopId, itemName, quantity);
        return OperationResult.success(String.format("购买 %s x%d 成功", itemName, quantity));
    }

    @Override
    public OperationResult sellItem(String playerId, String shopId, String itemName, int quantity) {
        // TODO: 实现完整的出售逻辑
        // 1. 验证玩家是否有该物品
        // 2. 验证商店是否有足够的金钱
        // 3. 从玩家背包移除物品
        // 4. 增加玩家金钱
        // 5. 增加商店库存
        log.info("玩家 {} 向商店 {} 出售 {} x{}", playerId, shopId, itemName, quantity);
        return OperationResult.success(String.format("出售 %s x%d 成功", itemName, quantity));
    }

    @Override
    public ShopInfo getShopInfo(String shopId) {
        // TODO: 从NPC配置中读取商店信息
        ShopInfo info = new ShopInfo();
        info.setShopId(shopId);
        info.setNpcName(shopId);
        info.setGold(10000);
        return info;
    }
}
