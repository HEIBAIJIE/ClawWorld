package com.heibai.clawworld.domain.service;

import com.heibai.clawworld.domain.combat.CombatCharacter;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.EnemyConfig;
import com.heibai.clawworld.infrastructure.config.data.character.EnemyLootConfig;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 战利品计算器
 * 根据设计文档计算战斗结束后的战利品
 */
public class CombatRewardCalculator {

    private final Random random;
    private final ConfigDataManager configDataManager;

    public CombatRewardCalculator() {
        this.random = new Random();
        this.configDataManager = null;
    }

    public CombatRewardCalculator(Random random) {
        this.random = random;
        this.configDataManager = null;
    }

    public CombatRewardCalculator(ConfigDataManager configDataManager) {
        this.random = new Random();
        this.configDataManager = configDataManager;
    }

    public CombatRewardCalculator(Random random, ConfigDataManager configDataManager) {
        this.random = random;
        this.configDataManager = configDataManager;
    }

    /**
     * 计算击败敌人的战利品
     * 根据设计文档：
     * - 敌人被击败后，会掉落战利品，包括经验、金钱和物品
     * - 战利品归属于对敌人造成最后攻击的队伍
     */
    public CombatReward calculateEnemyReward(CombatCharacter enemy, String winnerPartyId) {
        CombatReward reward = new CombatReward();
        reward.setPartyId(winnerPartyId);

        // 从敌人配置中获取掉落信息
        if (configDataManager != null && enemy.getEnemyConfigId() != null) {
            EnemyConfig enemyConfig = configDataManager.getEnemy(enemy.getEnemyConfigId());
            if (enemyConfig != null) {
                // 随机经验值（在配置的范围内）
                int exp = enemyConfig.getExpMin() + random.nextInt(enemyConfig.getExpMax() - enemyConfig.getExpMin() + 1);
                reward.setExperience(exp);

                // 随机金钱（在配置的范围内）
                int gold = enemyConfig.getGoldMin() + random.nextInt(enemyConfig.getGoldMax() - enemyConfig.getGoldMin() + 1);
                reward.setGold(gold);

                // 计算物品掉落
                List<String> droppedItems = new ArrayList<>();
                List<EnemyLootConfig> lootConfigs = configDataManager.getEnemyLoot(enemy.getEnemyConfigId());
                if (lootConfigs != null) {
                    for (EnemyLootConfig lootConfig : lootConfigs) {
                        // 根据掉落率判断是否掉落
                        if (random.nextDouble() < lootConfig.getDropRate()) {
                            droppedItems.add(lootConfig.getItemId());
                        }
                    }
                }
                reward.setItems(droppedItems);
            } else {
                // 配置不存在，使用简化计算
                useSimplifiedReward(enemy, reward);
            }
        } else {
            // 没有配置管理器，使用简化计算
            useSimplifiedReward(enemy, reward);
        }

        return reward;
    }

    /**
     * 使用简化的奖励计算（当配置不可用时）
     */
    private void useSimplifiedReward(CombatCharacter enemy, CombatReward reward) {
        int baseExp = enemy.getMaxHealth() * 2;
        int baseGold = enemy.getMaxHealth();
        reward.setExperience(baseExp);
        reward.setGold(baseGold);
        reward.setItems(new ArrayList<>());
    }

    /**
     * 计算玩家被击败的惩罚
     * 根据设计文档：
     * - 如果玩家被敌人击败，高于地图推荐等级的玩家掉落5%金钱，否则无惩罚
     * - 如果玩家被玩家击败，如果败方平均等级超过地图推荐等级，败方5%的金钱会被胜利方平分
     */
    public PlayerDefeatPenalty calculatePlayerDefeatPenalty(CombatCharacter player, boolean defeatedByEnemy,
                                                            int playerLevel, int mapRecommendedLevel, int playerGold) {
        PlayerDefeatPenalty penalty = new PlayerDefeatPenalty();
        penalty.setPlayerId(player.getCharacterId());

        // 从玩家数据中获取金钱并计算惩罚
        if (playerLevel > mapRecommendedLevel) {
            int goldLost = (int) (playerGold * 0.05); // 5%的金钱
            penalty.setGoldLost(goldLost);
            penalty.setHasPenalty(true);
        } else {
            penalty.setGoldLost(0);
            penalty.setHasPenalty(false);
        }

        return penalty;
    }

    /**
     * 战利品
     */
    @Data
    public static class CombatReward {
        private String partyId; // 获得战利品的队伍ID
        private int experience;
        private int gold;
        private List<String> items; // 物品ID列表
    }

    /**
     * 玩家失败惩罚
     */
    @Data
    public static class PlayerDefeatPenalty {
        private String playerId;
        private int goldLost;
        private boolean hasPenalty;
    }
}
