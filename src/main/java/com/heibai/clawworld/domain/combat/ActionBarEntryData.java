package com.heibai.clawworld.domain.combat;

import com.heibai.clawworld.domain.constants.GameConstants;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * 行动条条目数据（用于显示和持久化）
 */
@Getter
@Setter
public class ActionBarEntryData {
    private String characterId;
    private int progress; // 当前进度值（0-ACTION_BAR_THRESHOLD）

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
        return progress >= GameConstants.ACTION_BAR_THRESHOLD;
    }

    /**
     * 重置行动条
     */
    public void reset() {
        progress -= GameConstants.ACTION_BAR_THRESHOLD;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActionBarEntryData that = (ActionBarEntryData) o;
        return Objects.equals(characterId, that.characterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(characterId);
    }
}
