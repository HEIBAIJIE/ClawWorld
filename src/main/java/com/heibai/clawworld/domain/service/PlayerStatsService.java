package com.heibai.clawworld.domain.service;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.character.Role;
import com.heibai.clawworld.domain.item.Equipment;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.RoleConfig;
import com.heibai.clawworld.infrastructure.persistence.mapper.ConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 玩家属性计算领域服务
 * 集中处理所有属性计算相关的业务逻辑，避免属性计算逻辑散落在各处
 *
 * 属性计算规则（根据设计文档）：
 * 最终数值 = 职业基础数值 + 四维影响 + 装备加成
 *
 * 四维影响规则：
 * - 力量：+2物理攻击，+1物理防御，+0.1%命中
 * - 敏捷：+2速度，+0.5%暴击率，+1%暴击伤害，+0.2%命中，+0.2%闪避
 * - 法力：+10法力上限，+3法术攻击，+1法术防御，+0.1%命中
 * - 体力：+15生命上限，+2物理防御，+0.5法术防御
 */
@Service
@RequiredArgsConstructor
public class PlayerStatsService {

    private final ConfigDataManager configDataManager;
    private final ConfigMapper configMapper;

    /**
     * 重新计算玩家的所有属性
     * 包括：职业基础属性 + 四维影响 + 装备加成
     *
     * @param player 玩家领域对象
     * @param role 职业配置
     */
    public void recalculateStats(Player player, Role role) {
        // 第一步：计算职业基础属性（根据等级）
        applyRoleBaseStats(player, role);

        // 第二步：计算最终属性（四维 + 装备）
        calculateFinalStats(player);
    }

    /**
     * 应用职业基础属性
     * 根据当前等级计算职业提供的基础属性
     *
     * @param player 玩家领域对象
     * @param role 职业配置
     */
    private void applyRoleBaseStats(Player player, Role role) {
        int currentLevel = player.getLevel();

        player.setBaseMaxHealth((int)(role.getBaseHealth() + role.getHealthPerLevel() * (currentLevel - 1)));
        player.setBaseMaxMana((int)(role.getBaseMana() + role.getManaPerLevel() * (currentLevel - 1)));
        player.setBasePhysicalAttack((int)(role.getBasePhysicalAttack() + role.getPhysicalAttackPerLevel() * (currentLevel - 1)));
        player.setBasePhysicalDefense((int)(role.getBasePhysicalDefense() + role.getPhysicalDefensePerLevel() * (currentLevel - 1)));
        player.setBaseMagicAttack((int)(role.getBaseMagicAttack() + role.getMagicAttackPerLevel() * (currentLevel - 1)));
        player.setBaseMagicDefense((int)(role.getBaseMagicDefense() + role.getMagicDefensePerLevel() * (currentLevel - 1)));
        player.setBaseSpeed((int)(role.getBaseSpeed() + role.getSpeedPerLevel() * (currentLevel - 1)));
        player.setBaseCritRate(role.getBaseCritRate() + role.getCritRatePerLevel() * (currentLevel - 1));
        player.setBaseCritDamage(role.getBaseCritDamage() + role.getCritDamagePerLevel() * (currentLevel - 1));
        player.setBaseHitRate(role.getBaseHitRate() + role.getHitRatePerLevel() * (currentLevel - 1));
        player.setBaseDodgeRate(role.getBaseDodgeRate() + role.getDodgeRatePerLevel() * (currentLevel - 1));
    }

    /**
     * 计算最终战斗属性
     * 从基础属性开始，叠加四维影响和装备加成
     *
     * @param player 玩家领域对象
     */
    private void calculateFinalStats(Player player) {
        // 从职业基础属性开始
        int maxHealth = player.getBaseMaxHealth();
        int maxMana = player.getBaseMaxMana();
        int physicalAttack = player.getBasePhysicalAttack();
        int physicalDefense = player.getBasePhysicalDefense();
        int magicAttack = player.getBaseMagicAttack();
        int magicDefense = player.getBaseMagicDefense();
        int speed = player.getBaseSpeed();
        double critRate = player.getBaseCritRate();
        double critDamage = player.getBaseCritDamage();
        double hitRate = player.getBaseHitRate();
        double dodgeRate = player.getBaseDodgeRate();

        // 计算总四维（玩家自身 + 装备提供）
        int totalStrength = player.getStrength();
        int totalAgility = player.getAgility();
        int totalIntelligence = player.getIntelligence();
        int totalVitality = player.getVitality();

        if (player.getEquipment() != null) {
            for (Equipment eq : player.getEquipment().values()) {
                if (eq != null) {
                    totalStrength += eq.getStrength();
                    totalAgility += eq.getAgility();
                    totalIntelligence += eq.getIntelligence();
                    totalVitality += eq.getVitality();
                }
            }
        }

        // 应用四维对战斗属性的影响
        // 力量：+2物理攻击，+1物理防御，+0.1%命中
        physicalAttack += totalStrength * 2;
        physicalDefense += totalStrength;
        hitRate += totalStrength * 0.001;

        // 敏捷：+2速度，+0.5%暴击率，+1%暴击伤害，+0.2%命中，+0.2%闪避
        speed += totalAgility * 2;
        critRate += totalAgility * 0.005;
        critDamage += totalAgility * 0.01;
        hitRate += totalAgility * 0.002;
        dodgeRate += totalAgility * 0.002;

        // 法力：+10法力上限，+3法术攻击，+1法术防御，+0.1%命中
        maxMana += totalIntelligence * 10;
        magicAttack += totalIntelligence * 3;
        magicDefense += totalIntelligence;
        hitRate += totalIntelligence * 0.001;

        // 体力：+15生命上限，+2物理防御，+0.5法术防御
        maxHealth += totalVitality * 15;
        physicalDefense += totalVitality * 2;
        magicDefense += (int)(totalVitality * 0.5);

        // 累加装备提供的直接战斗属性
        if (player.getEquipment() != null) {
            for (Equipment eq : player.getEquipment().values()) {
                if (eq != null) {
                    physicalAttack += eq.getPhysicalAttack();
                    physicalDefense += eq.getPhysicalDefense();
                    magicAttack += eq.getMagicAttack();
                    magicDefense += eq.getMagicDefense();
                    speed += eq.getSpeed();
                    critRate += eq.getCritRate();
                    critDamage += eq.getCritDamage();
                    hitRate += eq.getHitRate();
                    dodgeRate += eq.getDodgeRate();
                }
            }
        }

        // 设置最终属性
        player.setMaxHealth(maxHealth);
        player.setMaxMana(maxMana);
        player.setPhysicalAttack(physicalAttack);
        player.setPhysicalDefense(physicalDefense);
        player.setMagicAttack(magicAttack);
        player.setMagicDefense(magicDefense);
        player.setSpeed(speed);
        player.setCritRate(critRate);
        player.setCritDamage(critDamage);
        player.setHitRate(hitRate);
        player.setDodgeRate(dodgeRate);
    }

    /**
     * 便捷方法：根据玩家的 roleId 自动获取职业配置并重新计算属性
     *
     * @param player 玩家领域对象
     */
    public void recalculateStats(Player player) {
        RoleConfig roleConfig = configDataManager.getRole(player.getRoleId());
        if (roleConfig != null) {
            Role role = configMapper.toDomain(roleConfig);
            recalculateStats(player, role);
        }
    }

    /**
     * 获取玩家的职业配置
     *
     * @param roleId 职业ID
     * @return 职业领域对象，如果不存在返回null
     */
    public Role getRole(String roleId) {
        RoleConfig roleConfig = configDataManager.getRole(roleId);
        return configMapper.toDomain(roleConfig);
    }
}
