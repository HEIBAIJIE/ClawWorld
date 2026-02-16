package com.heibai.clawworld.infrastructure.persistence.repository;

import com.heibai.clawworld.infrastructure.persistence.entity.ChatMessageEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天记录持久化仓储接口
 */
@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessageEntity, String> {

    /**
     * 查询服务器频道最近的消息
     */
    List<ChatMessageEntity> findByChannelTypeOrderByTimestampDesc(
            ChatMessageEntity.ChannelType channelType);

    /**
     * 查询地图频道最近的消息
     */
    List<ChatMessageEntity> findByChannelTypeAndMapIdOrderByTimestampDesc(
            ChatMessageEntity.ChannelType channelType, String mapId);

    /**
     * 查询队伍频道最近的消息
     */
    List<ChatMessageEntity> findByChannelTypeAndPartyIdOrderByTimestampDesc(
            ChatMessageEntity.ChannelType channelType, String partyId);

    /**
     * 查询私聊消息（双向）
     */
    @Query("{ 'channelType': ?0, $or: [ { 'senderId': ?1, 'receiverId': ?2 }, { 'senderId': ?2, 'receiverId': ?1 } ] }")
    List<ChatMessageEntity> findPrivateMessages(
            ChatMessageEntity.ChannelType channelType, String playerId1, String playerId2);

    /**
     * 查询指定时间之后的消息
     */
    List<ChatMessageEntity> findByTimestampAfterOrderByTimestampAsc(Long timestamp);
}
