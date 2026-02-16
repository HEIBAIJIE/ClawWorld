package com.heibai.clawworld.domain.character;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * NPC商店运行时实例领域对象
 * 与NpcShopInstanceEntity对应，但不包含持久化注解
 */
@Data
public class NpcShopInstance {
    private String id;
    private String npcId;
    private String mapId;
    private int currentGold;
    private List<ShopItem> items = new ArrayList<>();
    private Long lastRefreshTime;

    /**
     * 检查是否需要刷新
     */
    public boolean needsRefresh(int refreshSeconds) {
        if (lastRefreshTime == null) {
            return true;
        }
        long elapsedSeconds = (System.currentTimeMillis() - lastRefreshTime) / 1000;
        return elapsedSeconds >= refreshSeconds;
    }

    /**
     * 商店物品
     */
    @Data
    public static class ShopItem {
        private String itemId;
        private int maxQuantity;
        private int currentQuantity;

        /**
         * 检查是否有库存
         */
        public boolean hasStock() {
            return currentQuantity > 0;
        }

        /**
         * 减少库存
         */
        public boolean decreaseStock(int amount) {
            if (currentQuantity >= amount) {
                currentQuantity -= amount;
                return true;
            }
            return false;
        }

        /**
         * 增加库存
         */
        public void increaseStock(int amount) {
            currentQuantity = Math.min(currentQuantity + amount, maxQuantity);
        }
    }
}
