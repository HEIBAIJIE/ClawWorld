package com.heibai.clawworld.domain.combat;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗领域对象
 * 与CombatEntity对应，但不包含持久化注解
 */
@Data
public class Combat {
    private String id;
    private String mapId;
    private Long startTime;
    private CombatStatus status;
    private List<CombatParty> parties = new ArrayList<>();
    private List<ActionBarEntry> actionBar = new ArrayList<>();
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
     * 检查战斗是否超时（10分钟）
     */
    public boolean isTimeout() {
        if (startTime == null) {
            return false;
        }
        long elapsedMillis = System.currentTimeMillis() - startTime;
        return elapsedMillis > 10 * 60 * 1000;
    }

    /**
     * 添加战斗日志
     */
    public void addLog(String log) {
        combatLog.add(log);
    }

    /**
     * 参战方
     */
    @Data
    public static class CombatParty {
        private String faction;
        private List<CombatCharacter> characters = new ArrayList<>();
    }

    /**
     * 战斗中的角色
     */
    @Data
    public static class CombatCharacter {
        private String characterId;
        private String characterType;
        private String name;
        private int currentHealth;
        private int maxHealth;
        private int currentMana;
        private int maxMana;
        private boolean isDead;
        private List<SkillCooldown> skillCooldowns = new ArrayList<>();

        /**
         * 检查是否存活
         */
        public boolean isAlive() {
            return !isDead && currentHealth > 0;
        }

        /**
         * 受到伤害
         */
        public void takeDamage(int damage) {
            currentHealth = Math.max(0, currentHealth - damage);
            if (currentHealth == 0) {
                isDead = true;
            }
        }

        /**
         * 恢复生命
         */
        public void heal(int amount) {
            if (!isDead) {
                currentHealth = Math.min(maxHealth, currentHealth + amount);
            }
        }
    }

    /**
     * 技能冷却
     */
    @Data
    public static class SkillCooldown {
        private String skillId;
        private int remainingTurns;

        /**
         * 减少冷却回合数
         */
        public void decreaseCooldown() {
            if (remainingTurns > 0) {
                remainingTurns--;
            }
        }

        /**
         * 检查技能是否可用
         */
        public boolean isReady() {
            return remainingTurns <= 0;
        }
    }

    /**
     * 行动条条目
     */
    @Data
    public static class ActionBarEntry {
        private String characterId;
        private int progress; // 当前进度值（0-10000）

        /**
         * 增加进度
         */
        public void increaseProgress(int speed) {
            progress += speed;
        }

        /**
         * 检查是否轮到行动
         */
        public boolean isReady() {
            return progress >= 10000;
        }

        /**
         * 重置行动条
         */
        public void reset() {
            progress -= 10000;
        }
    }
}
