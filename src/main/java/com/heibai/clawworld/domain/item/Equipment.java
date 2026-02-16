package com.heibai.clawworld.domain.item;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 装备领域对象 - 运行时使用
 * 装备是一种特殊的物品，不可堆叠
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Equipment extends Item {
    /**
     * 装备栏位
     */
    private EquipmentSlot slot;

    /**
     * 稀有度
     */
    private Rarity rarity;

    /**
     * 装备实例编号（例如：铁剑#1中的1）
     * 每件装备都有唯一的实例编号
     */
    private Long instanceNumber;

    // 四维属性加成
    private int strength;
    private int agility;
    private int intelligence;
    private int vitality;

    // 直接战斗属性加成
    private int physicalAttack;
    private int physicalDefense;
    private int magicAttack;
    private int magicDefense;
    private int speed;
    private double critRate;
    private double critDamage;
    private double hitRate;
    private double dodgeRate;

    public Equipment() {
        // 装备类型固定为EQUIPMENT，不可堆叠
        super();
        setType(ItemType.EQUIPMENT);
        setMaxStack(1);
    }

    /**
     * 获取装备的完整显示名称（包含实例编号）
     * 例如：铁剑#1
     */
    public String getDisplayName() {
        if (instanceNumber != null) {
            return getName() + "#" + instanceNumber;
        }
        return getName();
    }

    public enum EquipmentSlot {
        HEAD, CHEST, LEGS, FEET, LEFT_HAND, RIGHT_HAND, ACCESSORY1, ACCESSORY2
    }
}
