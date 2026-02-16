package com.heibai.clawworld.domain.character;

import lombok.Data;

/**
 * 敌人运行时实例领域对象
 * 与EnemyInstanceEntity对应，但不包含持久化注解
 */
@Data
public class EnemyInstance {
    private String id;
    private String mapId;
    private String instanceId;
    private String templateId;
    private String displayName; // 显示名称（包含编号，如"哥布林#1"）

    // 运行时状态
    private int currentHealth;
    private int currentMana;
    private boolean isDead;
    private Long lastDeathTime;

    // 战斗状态
    private boolean inCombat;
    private String combatId;

    // 位置
    private int x;
    private int y;

    /**
     * 检查是否需要刷新
     */
    public boolean needsRespawn(int respawnSeconds) {
        if (!isDead || lastDeathTime == null) {
            return false;
        }
        long elapsedSeconds = (System.currentTimeMillis() - lastDeathTime) / 1000;
        return elapsedSeconds >= respawnSeconds;
    }

    /**
     * 是否可通过
     * 根据设计文档：在消灭敌人以前，无法进入敌人所在的格子
     */
    public boolean isPassable() {
        return isDead;
    }
}
