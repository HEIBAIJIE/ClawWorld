package com.heibai.clawworld.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 聊天记录持久化实体
 * 根据设计文档：聊天记录在服务器保留5分钟
 */
@Data
@Document(collection = "chat_messages")
public class ChatMessageEntity {
    @Id
    private String id;

    /**
     * 聊天频道类型
     */
    private ChannelType channelType;

    /**
     * 发送者玩家ID
     */
    private String senderId;

    /**
     * 发送者昵称
     */
    private String senderNickname;

    /**
     * 接收者玩家ID（仅私聊时使用）
     */
    private String receiverId;

    /**
     * 地图ID（仅地图频道时使用）
     */
    private String mapId;

    /**
     * 队伍ID（仅队伍频道时使用）
     */
    private String partyId;

    /**
     * 消息内容（最多30字）
     */
    private String message;

    /**
     * 发送时间（毫秒时间戳）
     * 添加索引以支持按时间查询和自动过期
     */
    @Indexed(expireAfterSeconds = 300) // 5分钟后自动删除
    private Long timestamp;

    /**
     * 聊天频道类型
     */
    public enum ChannelType {
        WORLD,      // 服务器频道
        MAP,        // 地图频道
        PARTY,      // 队伍频道
        PRIVATE     // 私聊
    }
}
