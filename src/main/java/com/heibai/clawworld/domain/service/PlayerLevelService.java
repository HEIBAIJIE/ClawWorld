package com.heibai.clawworld.domain.service;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.character.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 玩家升级领域服务
 * 集中处理所有升级相关的业务逻辑，避免升级逻辑散落在各处
 *
 * 升级规则（根据设计文档）：
 * - 升级所需经验公式: exp = 50 * level^2 + 50 * level
 * - 升级后获得5个可自由分配的属性点
 * - 升级后职业基础属性按等级成长
 * - 升级后回满生命和法力
 */
@Service
@RequiredArgsConstructor
public class PlayerLevelService {

    private final PlayerStatsService playerStatsService;

    /**
     * 检查并处理玩家升级
     * 支持连续升级（如果经验足够升多级）
     *
     * @param player 玩家领域对象
     * @param role 职业配置
     * @return 是否发生了升级
     */
    public boolean processLevelUp(Player player, Role role) {
        int requiredExp = com.heibai.clawworld.domain.character.Character.calculateExperienceForLevel(player.getLevel());
        boolean leveledUp = false;

        while (player.getExperience() >= requiredExp) {
            // 扣除升级所需经验
            player.setExperience(player.getExperience() - requiredExp);

            // 升级
            int oldLevel = player.getLevel();
            player.setLevel(oldLevel + 1);

            // 获得5个属性点
            player.setFreeAttributePoints(player.getFreeAttributePoints() + 5);

            leveledUp = true;

            // 计算下一级所需经验
            requiredExp = com.heibai.clawworld.domain.character.Character.calculateExperienceForLevel(player.getLevel());
        }

        if (leveledUp) {
            // 重新计算职业基础属性和最终属性
            playerStatsService.recalculateStats(player, role);

            // 升级后回满生命和法力
            player.setCurrentHealth(player.getMaxHealth());
            player.setCurrentMana(player.getMaxMana());
        }

        return leveledUp;
    }

    /**
     * 增加经验值
     *
     * @param player 玩家领域对象
     * @param experience 增加的经验值
     */
    public void addExperience(Player player, int experience) {
        if (experience > 0) {
            player.setExperience(player.getExperience() + experience);
        }
    }

    /**
     * 增加经验值并检查升级
     *
     * @param player 玩家领域对象
     * @param role 职业配置
     * @param experience 增加的经验值
     * @return 是否发生了升级
     */
    public boolean addExperienceAndCheckLevelUp(Player player, Role role, int experience) {
        addExperience(player, experience);
        return processLevelUp(player, role);
    }

    /**
     * 便捷方法：根据玩家的 roleId 自动获取职业配置并处理升级
     *
     * @param player 玩家领域对象
     * @return 是否发生了升级
     */
    public boolean processLevelUp(Player player) {
        Role role = playerStatsService.getRole(player.getRoleId());
        if (role == null) {
            return false;
        }
        return processLevelUp(player, role);
    }

    /**
     * 便捷方法：增加经验值并检查升级（自动获取职业配置）
     *
     * @param player 玩家领域对象
     * @param experience 增加的经验值
     * @return 是否发生了升级
     */
    public boolean addExperienceAndCheckLevelUp(Player player, int experience) {
        addExperience(player, experience);
        return processLevelUp(player);
    }
}
