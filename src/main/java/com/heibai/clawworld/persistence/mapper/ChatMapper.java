package com.heibai.clawworld.persistence.mapper;

import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.persistence.entity.ChatMessageEntity;
import org.springframework.stereotype.Component;

/**
 * 聊天消息领域对象与持久化实体之间的映射器
 */
@Component
public class ChatMapper {

    /**
     * 将领域对象转换为持久化实体
     */
    public ChatMessageEntity toEntity(ChatMessage message) {
        if (message == null) {
            return null;
        }

        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setId(message.getId());
        entity.setChannelType(ChatMessageEntity.ChannelType.valueOf(message.getChannelType().name()));
        entity.setSenderId(message.getSenderId());
        entity.setSenderNickname(message.getSenderNickname());
        entity.setReceiverId(message.getReceiverId());
        entity.setMapId(message.getMapId());
        entity.setPartyId(message.getPartyId());
        entity.setMessage(message.getMessage());
        entity.setTimestamp(message.getTimestamp());
        return entity;
    }

    /**
     * 将持久化实体转换为领域对象
     */
    public ChatMessage toDomain(ChatMessageEntity entity) {
        if (entity == null) {
            return null;
        }

        ChatMessage message = new ChatMessage();
        message.setId(entity.getId());
        message.setChannelType(ChatMessage.ChannelType.valueOf(entity.getChannelType().name()));
        message.setSenderId(entity.getSenderId());
        message.setSenderNickname(entity.getSenderNickname());
        message.setReceiverId(entity.getReceiverId());
        message.setMapId(entity.getMapId());
        message.setPartyId(entity.getPartyId());
        message.setMessage(entity.getMessage());
        message.setTimestamp(entity.getTimestamp());
        return message;
    }
}
