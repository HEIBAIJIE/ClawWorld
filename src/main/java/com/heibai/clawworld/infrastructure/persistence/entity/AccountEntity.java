package com.heibai.clawworld.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 账号持久化实体
 * 用于MongoDB存储，与领域对象Account分离
 */
@Data
@Document(collection = "accounts")
public class AccountEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String nickname;

    private String password;

    // 关联的玩家ID
    @Indexed
    private String playerId;

    // 会话信息
    @Indexed
    private String sessionId;
    private boolean online;
    private Long lastLoginTime;
    private Long lastLogoutTime;

    // 当前窗口状态
    private String currentWindowId;
    private String currentWindowType; // REGISTER, MAP, COMBAT, TRADE

    // 最后一次获取状态的时间戳（用于追踪环境变化）
    private Long lastStateTimestamp;

    // 最后一次执行的指令（用于在状态中显示）
    private String lastCommand;
    private Long lastCommandTimestamp;

    // 上次状态的实体快照（用于追踪实体变化）
    // 格式：Map<entityName, EntitySnapshot>
    // EntitySnapshot包含：位置(x,y)和交互选项列表
    private java.util.Map<String, EntitySnapshot> lastEntitySnapshot;

    @Data
    public static class EntitySnapshot {
        private int x;
        private int y;
        private java.util.List<String> interactionOptions;
    }
}
