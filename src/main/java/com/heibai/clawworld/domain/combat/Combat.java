package com.heibai.clawworld.domain.combat;

import lombok.Getter;
import lombok.Setter;
import com.heibai.clawworld.domain.constants.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 战斗领域对象
 * 与CombatEntity对应，但不包含持久化注解
 */
@Getter
@Setter
public class Combat {
    private String id;
    private String mapId;
    private Long startTime;
    private CombatStatus status;
    private List<CombatPartyData> parties = new ArrayList<>();
    private List<ActionBarEntryData> actionBar = new ArrayList<>();
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
     * 检查战斗是否超时
     */
    public boolean isTimeout() {
        if (startTime == null) {
            return false;
        }
        long elapsedMillis = System.currentTimeMillis() - startTime;
        return elapsedMillis > GameConstants.COMBAT_TIMEOUT_MILLIS;
    }

    /**
     * 添加战斗日志
     */
    public void addLog(String log) {
        combatLog.add(log);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Combat combat = (Combat) o;
        return Objects.equals(id, combat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
