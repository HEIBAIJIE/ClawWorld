package com.heibai.clawworld.interfaces.log;

import com.heibai.clawworld.domain.character.NpcShopInstance;
import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.NpcConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 商店窗口日志生成器
 */
@Service
@RequiredArgsConstructor
public class ShopWindowLogGenerator {

    private final ConfigDataManager configDataManager;

    /**
     * 生成商店窗口日志
     */
    public void generateShopWindowLogs(GameLogBuilder builder, NpcShopInstance shop, Player player) {
        // 获取NPC配置
        NpcConfig npcConfig = configDataManager.getNpc(shop.getNpcId());
        String npcName = npcConfig != null ? npcConfig.getName() : "商店";

        // 1. 商店基本信息
        builder.addWindow("商店窗口", String.format("商店：%s", npcName));

        // 2. 商店出售的商品
        StringBuilder sellItems = new StringBuilder();
        sellItems.append("出售商品：\n");
        if (shop.getItems() != null && !shop.getItems().isEmpty()) {
            for (NpcShopInstance.ShopItem item : shop.getItems()) {
                String itemName = getItemName(item.getItemId());
                sellItems.append(String.format("- %s  库存:%d\n",
                    itemName,
                    item.getCurrentQuantity()));
            }
        } else {
            sellItems.append("(暂无商品)\n");
        }
        builder.addWindow("商店窗口", sellItems.toString());

        // 3. 商店收购信息
        StringBuilder buyInfo = new StringBuilder();
        buyInfo.append("收购信息：\n");
        if (npcConfig != null) {
            buyInfo.append("商店收购物品\n");
        } else {
            buyInfo.append("(不收购物品)\n");
        }
        builder.addWindow("商店窗口", buyInfo.toString());

        // 4. 玩家资产
        StringBuilder playerAssets = new StringBuilder();
        playerAssets.append("你的资产：\n");
        playerAssets.append(String.format("金币: %d\n", player.getGold()));
        playerAssets.append(String.format("背包空间: %d/50\n",
            player.getInventory() != null ? player.getInventory().size() : 0));
        builder.addWindow("商店窗口", playerAssets.toString());

        // 5. 可用指令
        builder.addWindow("商店窗口", "当前窗口可用指令：\n" +
            "shop buy [物品名称] [数量] - 购买商品\n" +
            "shop sell [物品名称] [数量] - 出售物品\n" +
            "shop leave - 离开商店");
    }

    /**
     * 生成商店状态日志
     */
    public void generateShopStateLogs(GameLogBuilder builder, NpcShopInstance shop, Player player, String commandResult) {
        // 1. 商店库存变化
        if (shop.getItems() != null && !shop.getItems().isEmpty()) {
            StringBuilder stockChanges = new StringBuilder();
            stockChanges.append("商店库存：\n");
            for (NpcShopInstance.ShopItem item : shop.getItems()) {
                String itemName = getItemName(item.getItemId());
                stockChanges.append(String.format("- %s  库存:%d\n", itemName, item.getCurrentQuantity()));
            }
            builder.addState("库存变化", stockChanges.toString());
        }

        // 2. 玩家资产变化
        builder.addState("你的资产", String.format("金币: %d  背包: %d/50",
            player.getGold(),
            player.getInventory() != null ? player.getInventory().size() : 0));

        // 3. 指令响应
        builder.addState("指令响应", commandResult + "执行完毕，" + commandResult);
    }

    /**
     * 获取物品名称
     */
    private String getItemName(String itemId) {
        var itemConfig = configDataManager.getItem(itemId);
        if (itemConfig != null) {
            return itemConfig.getName();
        }
        var equipConfig = configDataManager.getEquipment(itemId);
        if (equipConfig != null) {
            return equipConfig.getName();
        }
        return itemId;
    }
}
