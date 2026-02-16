package com.heibai.clawworld.combat.ai;

import com.heibai.clawworld.combat.CombatCharacter;
import com.heibai.clawworld.combat.CombatInstance;
import com.heibai.clawworld.service.ConfigDataManager;
import com.heibai.clawworld.config.skill.SkillConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * 简单敌人AI实现
 *
 * 决策逻辑：
 * 1. 根据设计文档的仇恨机制：优先攻击物理防御+法术防御更高的角色
 * 2. 选择可用的技能（不在冷却中，法力足够）
 * 3. 如果没有可用技能，使用普通攻击
 */
@Slf4j
public class SimpleEnemyAI implements EnemyAI {

    private final Random random = new Random();
    private final ConfigDataManager configDataManager;

    // 普通攻击技能ID
    private static final String BASIC_ATTACK_SKILL_ID = "普通攻击";

    public SimpleEnemyAI() {
        this.configDataManager = null; // 默认构造函数，用于不需要配置的场景
    }

    public SimpleEnemyAI(ConfigDataManager configDataManager) {
        this.configDataManager = configDataManager;
    }

    @Override
    public AIDecision makeDecision(CombatInstance combat, CombatCharacter enemy) {
        // 获取所有敌对目标
        List<CombatCharacter> targets = combat.getEnemyCharacters(enemy.getFactionId());

        if (targets.isEmpty()) {
            log.warn("敌人 {} 没有可攻击的目标", enemy.getName());
            return AIDecision.skip();
        }

        // 根据仇恨机制选择目标
        CombatCharacter target = selectTargetByThreat(targets);

        // 选择技能
        String skillId = selectSkill(enemy);

        log.debug("敌人 {} 决定使用技能 {} 攻击 {}", enemy.getName(), skillId, target.getName());

        return AIDecision.attack(skillId, target.getCharacterId());
    }

    /**
     * 根据仇恨机制选择目标
     * 根据设计文档：敌人会更倾向于攻击物理防御 + 法术防御更高的角色
     */
    private CombatCharacter selectTargetByThreat(List<CombatCharacter> targets) {
        return targets.stream()
            .max(Comparator.comparingInt(c -> c.getPhysicalDefense() + c.getMagicDefense()))
            .orElse(targets.get(0));
    }

    /**
     * 选择技能
     * 优先选择可用的技能，如果没有则使用普通攻击
     */
    private String selectSkill(CombatCharacter enemy) {
        // 获取所有技能
        List<String> skills = enemy.getSkillIds();

        if (skills == null || skills.isEmpty()) {
            return BASIC_ATTACK_SKILL_ID;
        }

        // 过滤出可用的技能（不在冷却中，法力足够）
        List<String> availableSkills = skills.stream()
            .filter(skillId -> !enemy.isSkillOnCooldown(skillId))
            .filter(skillId -> {
                // 从技能配置中获取法力消耗
                if (configDataManager != null) {
                    SkillConfig skillConfig = configDataManager.getSkill(skillId);
                    if (skillConfig != null) {
                        return enemy.getCurrentMana() >= skillConfig.getManaCost();
                    }
                }
                // 如果没有配置管理器或找不到技能配置，假设可用
                return true;
            })
            .toList();

        if (availableSkills.isEmpty()) {
            return BASIC_ATTACK_SKILL_ID;
        }

        // 随机选择一个可用技能
        return availableSkills.get(random.nextInt(availableSkills.size()));
    }
}
