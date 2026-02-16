package com.heibai.clawworld.domain.character;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.heibai.clawworld.domain.item.Equipment;
import com.heibai.clawworld.domain.item.Item;

import java.util.Map;

/**
 * 玩家领域对象（纯业务逻辑对象，不包含持久化注解）
 * 根据设计文档：玩家是一类特殊的角色，特殊之处在于他们的行为受控制
 * 玩家的最终数值 = 职业原始数值 + 四维影响 + 装备加成
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Player extends Character {
    private String id;
    private String username;
    private String nickname;
    private String password;
    private String roleId; // 职业ID

    // 四维属性（玩家自由分配的属性点）
    private int strength;
    private int agility;
    private int intelligence;
    private int vitality;
    private int freeAttributePoints;

    // 金钱
    private int gold;

    // 装备栏
    private Map<Equipment.EquipmentSlot, Equipment> equipment;

    // 背包（最多50类物品）
    // 注意：装备是一种特殊的物品，未装备的装备也存储在背包中
    private Map<String, ItemStack> inventory;

    // 位置信息
    private String currentMapId;

    // 队伍ID
    private String partyId;
    private boolean isPartyLeader;

    /**
     * 计算玩家的最终战斗属性
     * 最终数值 = 职业原始数值（已在Character中） + 四维影响 + 装备加成
     *
     * 四维影响规则（根据设计文档）：
     * - 力量：影响物理攻击力、物理防御力、（小幅）命中
     * - 敏捷：影响速度、暴击率、暴击伤害、命中、闪避
     * - 法力：影响法力值上限、法术攻击力、法术防御力、（小幅）命中
     * - 体力：影响生命值上限、物理防御力、（小幅）法术防御力
     */
    public void recalculateFinalStats() {
        // 基础值已经在Character中（来自职业配置）
        // 这里只需要叠加四维影响和装备加成

        int totalStrength = strength;
        int totalAgility = agility;
        int totalIntelligence = intelligence;
        int totalVitality = vitality;

        // 累加装备提供的四维
        if (equipment != null) {
            for (Equipment eq : equipment.values()) {
                if (eq != null) {
                    totalStrength += eq.getStrength();
                    totalAgility += eq.getAgility();
                    totalIntelligence += eq.getIntelligence();
                    totalVitality += eq.getVitality();
                }
            }
        }

        // 四维对战斗属性的影响
        // 力量：+2物理攻击，+1物理防御，+0.1%命中
        int strBonus = totalStrength * 2;
        setPhysicalAttack(getPhysicalAttack() + strBonus);
        setPhysicalDefense(getPhysicalDefense() + totalStrength);
        setHitRate(getHitRate() + totalStrength * 0.001);

        // 敏捷：+2速度，+0.5%暴击率，+1%暴击伤害，+0.2%命中，+0.2%闪避
        setSpeed(getSpeed() + totalAgility * 2);
        setCritRate(getCritRate() + totalAgility * 0.005);
        setCritDamage(getCritDamage() + totalAgility * 0.01);
        setHitRate(getHitRate() + totalAgility * 0.002);
        setDodgeRate(getDodgeRate() + totalAgility * 0.002);

        // 法力：+10法力上限，+3法术攻击，+1法术防御，+0.1%命中
        setMaxMana(getMaxMana() + totalIntelligence * 10);
        setMagicAttack(getMagicAttack() + totalIntelligence * 3);
        setMagicDefense(getMagicDefense() + totalIntelligence);
        setHitRate(getHitRate() + totalIntelligence * 0.001);

        // 体力：+15生命上限，+2物理防御，+0.5法术防御
        setMaxHealth(getMaxHealth() + totalVitality * 15);
        setPhysicalDefense(getPhysicalDefense() + totalVitality * 2);
        setMagicDefense(getMagicDefense() + (int)(totalVitality * 0.5));

        // 累加装备提供的直接战斗属性
        if (equipment != null) {
            for (Equipment eq : equipment.values()) {
                if (eq != null) {
                    setPhysicalAttack(getPhysicalAttack() + eq.getPhysicalAttack());
                    setPhysicalDefense(getPhysicalDefense() + eq.getPhysicalDefense());
                    setMagicAttack(getMagicAttack() + eq.getMagicAttack());
                    setMagicDefense(getMagicDefense() + eq.getMagicDefense());
                    setSpeed(getSpeed() + eq.getSpeed());
                    setCritRate(getCritRate() + eq.getCritRate());
                    setCritDamage(getCritDamage() + eq.getCritDamage());
                    setHitRate(getHitRate() + eq.getHitRate());
                    setDodgeRate(getDodgeRate() + eq.getDodgeRate());
                }
            }
        }
    }

    /**
     * 是否可通过
     */
    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public String getEntityType() {
        return "PLAYER";
    }

    @Data
    public static class ItemStack {
        private Item item;
        private int quantity;
    }
}
