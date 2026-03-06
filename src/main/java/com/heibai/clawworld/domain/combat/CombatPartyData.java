package com.heibai.clawworld.domain.combat;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 战斗参战方数据（用于显示和持久化）
 */
@Getter
@Setter
public class CombatPartyData {
    private String faction;
    private List<CombatCharacterData> characters = new ArrayList<>();
}
