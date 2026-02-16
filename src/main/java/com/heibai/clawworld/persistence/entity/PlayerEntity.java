package com.heibai.clawworld.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 玩家持久化实体
 * 用于MongoDB存储，与领域对象Player分离
 */
@Data
@Document(collection = "players")
public class PlayerEntity {
    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    @Indexed(unique = true)
    private String nickname;

    private String password;
    private String roleId;

    // 基础属性
    private int level;
    private int experience;

    // 四维属性
    private int strength;
    private int agility;
    private int intelligence;
    private int vitality;
    private int freeAttributePoints;

    // 生命和法力
    private int maxHealth;
    private int currentHealth;
    private int maxMana;
    private int currentMana;

    // 战斗属性（基础值，来自职业）
    private int physicalAttack;
    private int physicalDefense;
    private int magicAttack;
    private int magicDefense;
    private int speed;
    private double critRate;
    private double critDamage;
    private double hitRate;
    private double dodgeRate;

    // 金钱
    private int gold;

    // 装备栏（存储装备ID和实例编号）
    private Map<String, EquipmentSlotData> equipment = new HashMap<>();

    // 背包（存储物品ID和数量）
    private Map<String, ItemStackData> inventory = new HashMap<>();

    // 技能列表（存储技能ID）
    private List<String> skills;

    // 位置信息
    private String currentMapId;
    private int x;
    private int y;

    // 队伍信息
    private String partyId;
    private boolean isPartyLeader;

    // 战斗状态
    private boolean inCombat;
    private String combatId;

    // 在线状态
    private boolean online;
    private String sessionId;
    private Long lastLoginTime;
    private Long lastLogoutTime;

    /**
     * 装备栏数据
     */
    @Data
    public static class EquipmentSlotData {
        private String equipmentId; // 装备模板ID
        private Long instanceNumber; // 装备实例编号
    }

    /**
     * 物品堆叠数据
     */
    @Data
    public static class ItemStackData {
        private String itemId;
        private int quantity;
    }
}
