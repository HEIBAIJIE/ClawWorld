package com.heibai.clawworld.domain.combat;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 战斗角色数据（用于显示和持久化）
 */
@Getter
@Setter
public class CombatCharacterData {
    private String characterId;
    private String characterType;
    private String name;
    private int currentHealth;
    private int maxHealth;
    private int currentMana;
    private int maxMana;
    private int speed;
    private boolean isDead;
    private List<SkillCooldownData> skillCooldowns = new ArrayList<>();

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CombatCharacterData that = (CombatCharacterData) o;
        return Objects.equals(characterId, that.characterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(characterId);
    }
}
