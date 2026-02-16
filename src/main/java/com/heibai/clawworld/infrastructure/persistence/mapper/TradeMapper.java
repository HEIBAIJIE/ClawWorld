package com.heibai.clawworld.infrastructure.persistence.mapper;

import com.heibai.clawworld.domain.trade.Trade;
import com.heibai.clawworld.infrastructure.persistence.entity.TradeEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 交易领域对象与持久化实体之间的映射器
 */
@Component
public class TradeMapper {

    /**
     * 将领域对象转换为持久化实体
     */
    public TradeEntity toEntity(Trade trade) {
        if (trade == null) {
            return null;
        }

        TradeEntity entity = new TradeEntity();
        entity.setId(trade.getId());
        entity.setInitiatorId(trade.getInitiatorId());
        entity.setReceiverId(trade.getReceiverId());
        entity.setStatus(TradeEntity.TradeStatus.valueOf(trade.getStatus().name()));
        entity.setCreateTime(trade.getCreateTime());
        entity.setInitiatorLocked(trade.isInitiatorLocked());
        entity.setReceiverLocked(trade.isReceiverLocked());
        entity.setInitiatorConfirmed(trade.isInitiatorConfirmed());
        entity.setReceiverConfirmed(trade.isReceiverConfirmed());

        // 转换发起者提供物
        if (trade.getInitiatorOffer() != null) {
            entity.setInitiatorOffer(toEntityOffer(trade.getInitiatorOffer()));
        }

        // 转换接收者提供物
        if (trade.getReceiverOffer() != null) {
            entity.setReceiverOffer(toEntityOffer(trade.getReceiverOffer()));
        }

        return entity;
    }

    /**
     * 将持久化实体转换为领域对象
     */
    public Trade toDomain(TradeEntity entity) {
        if (entity == null) {
            return null;
        }

        Trade trade = new Trade();
        trade.setId(entity.getId());
        trade.setInitiatorId(entity.getInitiatorId());
        trade.setReceiverId(entity.getReceiverId());
        trade.setStatus(Trade.TradeStatus.valueOf(entity.getStatus().name()));
        trade.setCreateTime(entity.getCreateTime());
        trade.setInitiatorLocked(entity.isInitiatorLocked());
        trade.setReceiverLocked(entity.isReceiverLocked());
        trade.setInitiatorConfirmed(entity.isInitiatorConfirmed());
        trade.setReceiverConfirmed(entity.isReceiverConfirmed());

        // 转换发起者提供物
        if (entity.getInitiatorOffer() != null) {
            trade.setInitiatorOffer(toDomainOffer(entity.getInitiatorOffer()));
        }

        // 转换接收者提供物
        if (entity.getReceiverOffer() != null) {
            trade.setReceiverOffer(toDomainOffer(entity.getReceiverOffer()));
        }

        return trade;
    }

    /**
     * 转换交易提供物（领域对象 -> 实体）
     */
    private TradeEntity.TradeOffer toEntityOffer(Trade.TradeOffer offer) {
        TradeEntity.TradeOffer entityOffer = new TradeEntity.TradeOffer();
        entityOffer.setGold(offer.getGold());

        if (offer.getItems() != null) {
            entityOffer.setItems(offer.getItems().stream()
                .map(this::toEntityItem)
                .collect(Collectors.toList()));
        }

        return entityOffer;
    }

    /**
     * 转换交易提供物（实体 -> 领域对象）
     */
    private Trade.TradeOffer toDomainOffer(TradeEntity.TradeOffer entityOffer) {
        Trade.TradeOffer offer = new Trade.TradeOffer();
        offer.setGold(entityOffer.getGold());

        if (entityOffer.getItems() != null) {
            offer.setItems(entityOffer.getItems().stream()
                .map(this::toDomainItem)
                .collect(Collectors.toList()));
        }

        return offer;
    }

    /**
     * 转换交易物品（领域对象 -> 实体）
     */
    private TradeEntity.TradeItem toEntityItem(Trade.TradeItem item) {
        TradeEntity.TradeItem entityItem = new TradeEntity.TradeItem();
        entityItem.setItemId(item.getItemId());
        entityItem.setQuantity(item.getQuantity());
        entityItem.setEquipmentInstanceNumber(item.getEquipmentInstanceNumber());
        return entityItem;
    }

    /**
     * 转换交易物品（实体 -> 领域对象）
     */
    private Trade.TradeItem toDomainItem(TradeEntity.TradeItem entityItem) {
        Trade.TradeItem item = new Trade.TradeItem();
        item.setItemId(entityItem.getItemId());
        item.setQuantity(entityItem.getQuantity());
        item.setEquipmentInstanceNumber(entityItem.getEquipmentInstanceNumber());
        return item;
    }
}
