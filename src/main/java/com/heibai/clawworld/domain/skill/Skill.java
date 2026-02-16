package com.heibai.clawworld.domain.skill;

import lombok.Data;

/**
 * 技能领域对象 - 运行时使用
 * 只包含配置数据，不包含运行时状态
 */
@Data
public class Skill {
    private String id;
    private String name;
    private String description;
    private SkillTarget targetType;
    private DamageType damageType;
    private int manaCost;
    private int cooldown; // 冷却回合数

    private double damageMultiplier;

    public enum SkillTarget {
        SELF, ALLY_SINGLE, ALLY_ALL, ENEMY_SINGLE, ENEMY_ALL
    }

    public enum DamageType {
        PHYSICAL, MAGICAL, NONE
    }
}
