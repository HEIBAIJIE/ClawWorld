package com.heibai.clawworld.interfaces.log;

import com.heibai.clawworld.domain.combat.Combat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 战斗窗口日志生成器
 */
@Service
@RequiredArgsConstructor
public class CombatWindowLogGenerator {

    /**
     * 生成战斗窗口日志
     */
    public void generateCombatWindowLogs(GameLogBuilder builder, Combat combat, String playerId) {
        // 1. 战斗基本信息
        builder.addWindow("战斗窗口", "战斗开始！");

        // 2. 参战方信息
        StringBuilder partiesInfo = new StringBuilder();
        partiesInfo.append("参战方：\n");
        for (int i = 0; i < combat.getParties().size(); i++) {
            Combat.CombatParty party = combat.getParties().get(i);
            partiesInfo.append(String.format("第%d方：", i + 1));
            for (Combat.CombatCharacter character : party.getCharacters()) {
                partiesInfo.append(character.getName()).append(" ");
            }
            partiesInfo.append("\n");
        }
        builder.addWindow("战斗窗口", partiesInfo.toString());

        // 3. 所有参战角色状态
        StringBuilder participantsStatus = new StringBuilder();
        participantsStatus.append("角色状态：\n");
        for (Combat.CombatParty party : combat.getParties()) {
            for (Combat.CombatCharacter character : party.getCharacters()) {
                participantsStatus.append(String.format("%s - 生命%d/%d 法力%d/%d\n",
                    character.getName(),
                    character.getCurrentHealth(),
                    character.getMaxHealth(),
                    character.getCurrentMana(),
                    character.getMaxMana()));
            }
        }
        builder.addWindow("战斗窗口", participantsStatus.toString());

        // 4. 行动条顺序
        StringBuilder turnOrder = new StringBuilder();
        turnOrder.append("行动条顺序：\n");
        if (combat.getActionBar() != null && !combat.getActionBar().isEmpty()) {
            for (int i = 0; i < Math.min(5, combat.getActionBar().size()); i++) {
                Combat.ActionBarEntry entry = combat.getActionBar().get(i);
                String characterName = findCharacterName(combat, entry.getCharacterId());
                turnOrder.append(String.format("%d. %s (进度%d)\n", i + 1, characterName, entry.getProgress()));
            }
        }
        builder.addWindow("战斗窗口", turnOrder.toString());

        // 5. 当前玩家的技能
        Combat.CombatCharacter playerCharacter = findPlayerCharacter(combat, playerId);
        if (playerCharacter != null) {
            StringBuilder skills = new StringBuilder();
            skills.append("你的技能：\n");
            if (playerCharacter.getSkillCooldowns() != null && !playerCharacter.getSkillCooldowns().isEmpty()) {
                for (Combat.SkillCooldown cooldown : playerCharacter.getSkillCooldowns()) {
                    skills.append(String.format("- %s", cooldown.getSkillId()));
                    if (cooldown.getRemainingTurns() > 0) {
                        skills.append(String.format(" (冷却中: %d回合)", cooldown.getRemainingTurns()));
                    }
                    skills.append("\n");
                }
            } else {
                skills.append("- 普通攻击\n");
            }
            builder.addWindow("战斗窗口", skills.toString());
        }

        // 6. 可用指令
        builder.addWindow("战斗窗口", "当前窗口可用指令：\n" +
            "cast [技能名称] - 释放非指向技能\n" +
            "cast [技能名称] [目标名称] - 对目标释放技能\n" +
            "use [物品名称] - 使用物品\n" +
            "wait - 跳过回合\n" +
            "end - 逃离战斗（角色死亡）");
    }

    /**
     * 生成战斗状态日志
     */
    public void generateCombatStateLogs(GameLogBuilder builder, Combat combat, String playerId,
                                       String commandResult) {
        // 1. 战斗日志
        if (combat.getCombatLog() != null && !combat.getCombatLog().isEmpty()) {
            // 只显示最近的5条日志
            int startIndex = Math.max(0, combat.getCombatLog().size() - 5);
            for (int i = startIndex; i < combat.getCombatLog().size(); i++) {
                builder.addState("战斗日志", combat.getCombatLog().get(i));
            }
        }

        // 2. 角色状态变化
        StringBuilder statusChanges = new StringBuilder();
        for (Combat.CombatParty party : combat.getParties()) {
            for (Combat.CombatCharacter character : party.getCharacters()) {
                if (character.getCurrentHealth() <= 0) {
                    statusChanges.append(String.format("%s 已阵亡\n", character.getName()));
                } else {
                    statusChanges.append(String.format("%s - 生命%d/%d 法力%d/%d\n",
                        character.getName(),
                        character.getCurrentHealth(),
                        character.getMaxHealth(),
                        character.getCurrentMana(),
                        character.getMaxMana()));
                }
            }
        }
        if (statusChanges.length() > 0) {
            builder.addState("角色状态", statusChanges.toString());
        }

        // 3. 行动条更新
        if (combat.getActionBar() != null && !combat.getActionBar().isEmpty()) {
            StringBuilder turnOrderUpdate = new StringBuilder();
            for (int i = 0; i < Math.min(3, combat.getActionBar().size()); i++) {
                Combat.ActionBarEntry entry = combat.getActionBar().get(i);
                String characterName = findCharacterName(combat, entry.getCharacterId());
                turnOrderUpdate.append(String.format("%d. %s\n", i + 1, characterName));
            }
            builder.addState("行动条", turnOrderUpdate.toString());
        }

        // 4. 指令响应
        builder.addState("指令响应", commandResult + "执行完毕，" + commandResult);
    }

    /**
     * 查找玩家角色
     */
    private Combat.CombatCharacter findPlayerCharacter(Combat combat, String playerId) {
        for (Combat.CombatParty party : combat.getParties()) {
            for (Combat.CombatCharacter character : party.getCharacters()) {
                if (playerId.equals(character.getCharacterId())) {
                    return character;
                }
            }
        }
        return null;
    }

    /**
     * 根据角色ID查找角色名称
     */
    private String findCharacterName(Combat combat, String characterId) {
        for (Combat.CombatParty party : combat.getParties()) {
            for (Combat.CombatCharacter character : party.getCharacters()) {
                if (characterId.equals(character.getCharacterId())) {
                    return character.getName();
                }
            }
        }
        return characterId;
    }
}
