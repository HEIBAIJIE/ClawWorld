package com.heibai.clawworld.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易状态持久化实体
 * 根据设计文档：交易系统需要二次确认，流程为 发起交易 -> 放置物品 -> 锁定交易 -> 双方确认 -> 完成
 */
@Data
@Document(collection = "trades")
public class TradeEntity {
    @Id
    private String id;

    /**
     * 交易发起者玩家ID
     */
    private String initiatorId;

    /**
     * 交易接收者玩家ID
     */
    private String receiverId;

    /**
     * 交易状态
     */
    private TradeStatus status;

    /**
     * 交易创建时间（毫秒时间戳）
     */
    private Long createTime;

    /**
     * 发起者提供的物品
     */
    private TradeOffer initiatorOffer;

    /**
     * 接收者提供的物品
     */
    private TradeOffer receiverOffer;

    /**
     * 发起者是否锁定
     */
    private boolean initiatorLocked;

    /**
     * 接收者是否锁定
     */
    private boolean receiverLocked;

    /**
     * 发起者是否确认
     */
    private boolean initiatorConfirmed;

    /**
     * 接收者是否确认
     */
    private boolean receiverConfirmed;

    /**
     * 交易状态枚举
     */
    public enum TradeStatus {
        PENDING,        // 等待接受
        ACTIVE,         // 进行中
        COMPLETED,      // 已完成
        CANCELLED       // 已取消
    }

    /**
     * 交易提供物
     */
    @Data
    public static class TradeOffer {
        /**
         * 提供的金钱
         */
        private int gold;

        /**
         * 提供的物品列表
         */
        private List<TradeItem> items = new ArrayList<>();
    }

    /**
     * 交易物品
     */
    @Data
    public static class TradeItem {
        /**
         * 物品ID
         */
        private String itemId;

        /**
         * 数量
         */
        private int quantity;

        /**
         * 装备实例编号（仅装备时使用）
         */
        private Long equipmentInstanceNumber;
    }
}
