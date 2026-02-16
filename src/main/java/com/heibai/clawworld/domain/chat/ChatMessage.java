package com.heibai.clawworld.domain.chat;

import lombok.Data;

/**
 * 聊天消息领域对象
 * 与ChatMessageEntity对应，但不包含持久化注解
 */
@Data
public class ChatMessage {
    private String id;
    private ChannelType channelType;
    private String senderId;
    private String senderNickname;
    private String receiverId;
    private String mapId;
    private String partyId;
    private String message;
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

    /**
     * 检查消息是否过期（5分钟）
     */
    public boolean isExpired() {
        if (timestamp == null) {
            return false;
        }
        long elapsedMillis = System.currentTimeMillis() - timestamp;
        return elapsedMillis > 5 * 60 * 1000;
    }

    /**
     * 验证消息长度（最多30字）
     */
    public boolean isValidLength() {
        return message != null && message.length() <= 30;
    }
}
