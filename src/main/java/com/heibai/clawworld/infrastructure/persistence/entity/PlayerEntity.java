package com.heibai.clawworld.infrastructure.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
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

    // 职业基础属性（用于重新计算最终属性）
    private int baseMaxHealth;
    private int baseMaxMana;
    private int basePhysicalAttack;
    private int basePhysicalDefense;
    private int baseMagicAttack;
    private int baseMagicDefense;
    private int baseSpeed;
    private double baseCritRate;
    private double baseCritDamage;
    private double baseHitRate;
    private double baseDodgeRate;

    // 生命和法力（最终值）
    private int maxHealth;
    private int currentHealth;
    private int maxMana;
    private int currentMana;

    // 战斗属性（最终值）
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

    // 装备栏（使用枚举名称作为key）
    private Map<String, EquipmentSlotData> equipment = new HashMap<>();

    // 背包（统一存储普通物品和装备）
    private List<InventorySlotData> inventory = new ArrayList<>();

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

    /**
     * 装备栏数据
     */
    @Data
    public static class EquipmentSlotData {
        private String equipmentId; // 装备模板ID
        private Long instanceNumber; // 装备实例编号
    }

    /**
     * 背包槽位数据
     */
    @Data
    public static class InventorySlotData {
        private String type; // "ITEM" 或 "EQUIPMENT"
        private String itemId; // 物品或装备模板ID
        private int quantity; // 数量（装备固定为1）
        private Long equipmentInstanceNumber; // 装备实例编号（仅装备有效）
    }
}
