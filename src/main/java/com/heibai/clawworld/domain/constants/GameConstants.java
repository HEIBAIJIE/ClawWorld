package com.heibai.clawworld.domain.constants;

/**
 * 游戏常量
 * 集中管理游戏中的魔法数字，便于调整和维护
 */
public final class GameConstants {

    private GameConstants() {
        // 工具类，禁止实例化
    }

    // ==================== 战斗相关 ====================

    /**
     * 战斗超时时间（毫秒）
     */
    public static final long COMBAT_TIMEOUT_MILLIS = 10 * 60 * 1000; // 10分钟

    /**
     * 行动条满值阈值
     */
    public static final int ACTION_BAR_THRESHOLD = 10000;

    /**
     * 暴击基础伤害倍率
     */
    public static final double CRIT_BASE_MULTIPLIER = 1.5;

    // ==================== 经验和升级相关 ====================

    /**
     * 经验值计算公式：exp = A * level^2 + B * level + C
     * 二次项系数
     */
    public static final int EXP_COEFFICIENT_A = 50;

    /**
     * 经验值计算公式：exp = A * level^2 + B * level + C
     * 一次项系数
     */
    public static final int EXP_COEFFICIENT_B = 50;

    /**
     * 经验值计算公式：exp = A * level^2 + B * level + C
     * 常数项
     */
    public static final int EXP_COEFFICIENT_C = 0;

    /**
     * 升级获得的自由属性点
     */
    public static final int ATTRIBUTE_POINTS_PER_LEVEL = 5;

    // ==================== 背包相关 ====================

    /**
     * 背包最大容量（物品种类数）
     */
    public static final int INVENTORY_MAX_SLOTS = 50;

    // ==================== 四维属性影响系数 ====================

    /**
     * 力量对物理攻击的影响系数
     */
    public static final int STRENGTH_TO_PHYSICAL_ATTACK = 2;

    /**
     * 力量对物理防御的影响系数
     */
    public static final int STRENGTH_TO_PHYSICAL_DEFENSE = 1;

    /**
     * 力量对命中率的影响系数
     */
    public static final double STRENGTH_TO_HIT_RATE = 0.001;

    /**
     * 敏捷对速度的影响系数
     */
    public static final int AGILITY_TO_SPEED = 2;

    /**
     * 敏捷对暴击率的影响系数
     */
    public static final double AGILITY_TO_CRIT_RATE = 0.005;

    /**
     * 敏捷对暴击伤害的影响系数
     */
    public static final double AGILITY_TO_CRIT_DAMAGE = 0.01;

    /**
     * 敏捷对命中率的影响系数
     */
    public static final double AGILITY_TO_HIT_RATE = 0.002;

    /**
     * 敏捷对闪避率的影响系数
     */
    public static final double AGILITY_TO_DODGE_RATE = 0.002;

    /**
     * 智力对法力上限的影响系数
     */
    public static final int INTELLIGENCE_TO_MAX_MANA = 10;

    /**
     * 智力对法术攻击的影响系数
     */
    public static final int INTELLIGENCE_TO_MAGIC_ATTACK = 3;

    /**
     * 智力对法术防御的影响系数
     */
    public static final int INTELLIGENCE_TO_MAGIC_DEFENSE = 1;

    /**
     * 智力对命中率的影响系数
     */
    public static final double INTELLIGENCE_TO_HIT_RATE = 0.001;

    /**
     * 体力对生命上限的影响系数
     */
    public static final int VITALITY_TO_MAX_HEALTH = 15;

    /**
     * 体力对物理防御的影响系数
     */
    public static final int VITALITY_TO_PHYSICAL_DEFENSE = 2;

    /**
     * 体力对法术防御的影响系数
     */
    public static final double VITALITY_TO_MAGIC_DEFENSE = 0.5;
}
