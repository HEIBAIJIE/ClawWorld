package com.heibai.clawworld.domain.trade;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易领域对象
 * 与TradeEntity对应，但不包含持久化注解
 */
@Data
public class Trade {
    private String id;
    private String initiatorId;
    private String receiverId;
    private TradeStatus status;
    private Long createTime;
    private TradeOffer initiatorOffer;
    private TradeOffer receiverOffer;
    private boolean initiatorLocked;
    private boolean receiverLocked;
    private boolean initiatorConfirmed;
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
     * 检查双方是否都锁定
     */
    public boolean isBothLocked() {
        return initiatorLocked && receiverLocked;
    }

    /**
     * 检查双方是否都确认
     */
    public boolean isBothConfirmed() {
        return initiatorConfirmed && receiverConfirmed;
    }

    /**
     * 检查是否可以完成交易
     */
    public boolean canComplete() {
        return status == TradeStatus.ACTIVE && isBothLocked() && isBothConfirmed();
    }

    /**
     * 交易提供物
     */
    @Data
    public static class TradeOffer {
        private int gold;
        private List<TradeItem> items = new ArrayList<>();

        /**
         * 添加物品
         */
        public void addItem(String itemId, int quantity) {
            TradeItem item = new TradeItem();
            item.setItemId(itemId);
            item.setQuantity(quantity);
            items.add(item);
        }

        /**
         * 添加装备
         */
        public void addEquipment(String itemId, Long instanceNumber) {
            TradeItem item = new TradeItem();
            item.setItemId(itemId);
            item.setQuantity(1);
            item.setEquipmentInstanceNumber(instanceNumber);
            items.add(item);
        }

        /**
         * 移除物品
         */
        public boolean removeItem(String itemId) {
            return items.removeIf(item -> item.getItemId().equals(itemId));
        }
    }

    /**
     * 交易物品
     */
    @Data
    public static class TradeItem {
        private String itemId;
        private int quantity;
        private Long equipmentInstanceNumber;

        /**
         * 检查是否为装备
         */
        public boolean isEquipment() {
            return equipmentInstanceNumber != null;
        }
    }
}
