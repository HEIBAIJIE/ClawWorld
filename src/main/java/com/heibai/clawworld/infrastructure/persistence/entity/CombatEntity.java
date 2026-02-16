package com.heibai.clawworld.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗状态持久化实体
 * 根据设计文档：战斗采用CTB条件回合制，战斗状态放在内存中处理，只在战斗结束时异步持久化结果
 * 但为了支持服务器重启等场景，需要持久化战斗状态
 */
@Data
@Document(collection = "combats")
public class CombatEntity {
    @Id
    private String id;

    /**
     * 战斗所在地图ID
     */
    private String mapId;

    /**
     * 战斗开始时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 战斗状态
     */
    private CombatStatus status;

    /**
     * 参战方列表
     */
    private List<CombatParty> parties = new ArrayList<>();

    /**
     * 行动条状态（角色ID -> 当前行动条进度）
     */
    private List<ActionBarEntry> actionBar = new ArrayList<>();

    /**
     * 战斗日志
     */
    private List<String> combatLog = new ArrayList<>();

    /**
     * 战斗状态枚举
     */
    public enum CombatStatus {
        ONGOING,    // 进行中
        FINISHED,   // 已结束
        TIMEOUT     // 超时
    }

    /**
     * 参战方
     */
    @Data
    public static class CombatParty {
        /**
         * 阵营名称
         */
        private String faction;

        /**
         * 参战角色列表
         */
        private List<CombatCharacter> characters = new ArrayList<>();
    }

    /**
     * 战斗中的角色
     */
    @Data
    public static class CombatCharacter {
        /**
         * 角色ID
         */
        private String characterId;

        /**
         * 角色类型（PLAYER, ENEMY, NPC）
         */
        private String characterType;

        /**
         * 角色名称
         */
        private String name;

        /**
         * 当前生命值
         */
        private int currentHealth;

        /**
         * 最大生命值
         */
        private int maxHealth;

        /**
         * 当前法力值
         */
        private int currentMana;

        /**
         * 最大法力值
         */
        private int maxMana;

        /**
         * 是否已死亡
         */
        private boolean isDead;

        /**
         * 技能冷却状态（技能ID -> 剩余冷却回合数）
         */
        private List<SkillCooldown> skillCooldowns = new ArrayList<>();
    }

    /**
     * 技能冷却
     */
    @Data
    public static class SkillCooldown {
        private String skillId;
        private int remainingTurns;
    }

    /**
     * 行动条条目
     */
    @Data
    public static class ActionBarEntry {
        private String characterId;
        private int progress; // 当前进度值（0-10000）
    }
}
