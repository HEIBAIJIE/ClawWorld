package com.heibai.clawworld.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * 队伍持久化实体
 * 用于MongoDB存储，与领域对象PartyDomain分离
 */
@Data
@Document(collection = "parties")
public class PartyEntity {
    @Id
    private String id;

    /**
     * 队长的玩家ID
     */
    private String leaderId;

    /**
     * 队伍成员的玩家ID列表（包括队长）
     * 最多4个人
     */
    private List<String> memberIds = new ArrayList<>();

    /**
     * 队伍阵营
     * 队伍中所有成员共享同一个阵营
     */
    private String faction;

    /**
     * 队伍创建时间
     */
    private Long createdTime;

    /**
     * 待处理的组队邀请
     */
    private List<PartyInvitationData> pendingInvitations = new ArrayList<>();

    /**
     * 待处理的加入请求
     */
    private List<PartyRequestData> pendingRequests = new ArrayList<>();

    /**
     * 检查队伍是否已满
     */
    public boolean isFull() {
        return memberIds.size() >= 4;
    }

    /**
     * 检查是否为单人队伍
     */
    public boolean isSolo() {
        return memberIds.size() == 1;
    }

    /**
     * 检查玩家是否为队长
     */
    public boolean isLeader(String playerId) {
        return leaderId != null && leaderId.equals(playerId);
    }

    /**
     * 检查玩家是否在队伍中
     */
    public boolean hasMember(String playerId) {
        return memberIds.contains(playerId);
    }

    /**
     * 组队邀请数据
     */
    @Data
    public static class PartyInvitationData {
        private String inviterId;
        private String inviteeId;
        private Long inviteTime;
    }

    /**
     * 加入队伍请求数据
     */
    @Data
    public static class PartyRequestData {
        private String requesterId;
        private String partyId;
        private Long requestTime;
    }
}
