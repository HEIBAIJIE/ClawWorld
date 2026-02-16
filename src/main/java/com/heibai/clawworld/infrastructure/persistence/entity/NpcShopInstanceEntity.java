package com.heibai.clawworld.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * NPC商店运行时实例持久化实体
 * NPC的基础配置从CSV读取，但商店的运行时状态需要持久化
 * 根据设计文档：商品的数量和NPC资金5分钟刷新一次
 */
@Data
@Document(collection = "npc_shop_instances")
public class NpcShopInstanceEntity {
    @Id
    private String id;

    /**
     * NPC ID
     */
    @Indexed(unique = true)
    private String npcId;

    /**
     * 所在地图ID
     */
    private String mapId;

    /**
     * 当前金钱
     */
    private int currentGold;

    /**
     * 商店物品列表
     */
    private List<ShopItemData> items = new ArrayList<>();

    /**
     * 上次刷新时间（毫秒时间戳）
     */
    private Long lastRefreshTime;

    /**
     * 商店物品数据
     */
    @Data
    public static class ShopItemData {
        /**
         * 物品ID
         */
        private String itemId;

        /**
         * 最大数量（从配置读取）
         */
        private int maxQuantity;

        /**
         * 当前数量
         */
        private int currentQuantity;
    }
}
