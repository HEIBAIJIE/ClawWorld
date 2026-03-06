package com.heibai.clawworld.domain.combat;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 技能冷却数据（用于显示和持久化）
 */
@Getter
@Setter
public class SkillCooldownData {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SkillCooldownData that = (SkillCooldownData) o;
        return Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(skillId);
    }
}
