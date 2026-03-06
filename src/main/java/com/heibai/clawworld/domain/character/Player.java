package com.heibai.clawworld.domain.character;

import lombok.Getter;
import lombok.Setter;
import com.heibai.clawworld.domain.item.Equipment;
import com.heibai.clawworld.domain.item.Item;
import com.heibai.clawworld.domain.constants.GameConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 玩家领域对象（纯业务逻辑对象，不包含持久化注解）
 * 根据设计文档：玩家是一类特殊的角色，特殊之处在于他们的行为受控制
 * 玩家的最终数值 = 职业原始数值 + 四维影响 + 装备加成
 */
@Getter
@Setter
public class Player extends Character {
    private String id;
    private String roleId; // 职业ID

    // 四维属性（玩家自由分配的属性点）
    private int strength;
    private int agility;
    private int intelligence;
    private int vitality;
    private int freeAttributePoints;

    // 金钱
    private int gold;

    // 装备栏
    private Map<Equipment.EquipmentSlot, Equipment> equipment;

    // 背包（最多50类物品）
    // 统一存储普通物品和装备，通过类型区分
    private List<InventorySlot> inventory;

    /**
     * 添加物品到背包
     * @return true如果添加成功，false如果背包已满
     */
    public boolean addToInventory(InventorySlot slot) {
        if (inventory == null) {
            inventory = new ArrayList<>();
        }
        if (inventory.size() >= GameConstants.INVENTORY_MAX_SLOTS) {
            return false;
        }
        inventory.add(slot);
        return true;
    }

    // 队伍ID
    private String partyId;
    private boolean isPartyLeader;

    // 交易状态
    private String tradeId;

    // 商店状态
    private String currentShopId;

    /**
     * 从职业配置初始化基础属性
     * 在创建玩家或升级时调用
     */
    public void applyRoleStats(Role role) {
        // 计算当前等级的职业基础属性
        int currentLevel = getLevel();

        setBaseMaxHealth((int)(role.getBaseHealth() + role.getHealthPerLevel() * (currentLevel - 1)));
        setBaseMaxMana((int)(role.getBaseMana() + role.getManaPerLevel() * (currentLevel - 1)));
        setBasePhysicalAttack((int)(role.getBasePhysicalAttack() + role.getPhysicalAttackPerLevel() * (currentLevel - 1)));
        setBasePhysicalDefense((int)(role.getBasePhysicalDefense() + role.getPhysicalDefensePerLevel() * (currentLevel - 1)));
        setBaseMagicAttack((int)(role.getBaseMagicAttack() + role.getMagicAttackPerLevel() * (currentLevel - 1)));
        setBaseMagicDefense((int)(role.getBaseMagicDefense() + role.getMagicDefensePerLevel() * (currentLevel - 1)));
        setBaseSpeed((int)(role.getBaseSpeed() + role.getSpeedPerLevel() * (currentLevel - 1)));
        setBaseCritRate(role.getBaseCritRate() + role.getCritRatePerLevel() * (currentLevel - 1));
        setBaseCritDamage(role.getBaseCritDamage() + role.getCritDamagePerLevel() * (currentLevel - 1));
        setBaseHitRate(role.getBaseHitRate() + role.getHitRatePerLevel() * (currentLevel - 1));
        setBaseDodgeRate(role.getBaseDodgeRate() + role.getDodgeRatePerLevel() * (currentLevel - 1));

        // 应用基础属性后重新计算最终属性
        recalculateFinalStats();
    }

    /**
     * 玩家升级
     * 根据设计文档：升级后获得可以自由分配的属性点
     */
    public void levelUp(Role role) {
        setLevel(getLevel() + 1);
        setFreeAttributePoints(getFreeAttributePoints() + GameConstants.ATTRIBUTE_POINTS_PER_LEVEL);

        // 重新应用职业属性（包含新等级的加成）
        applyRoleStats(role);
    }

    /**
     * 计算玩家的最终战斗属性
     * 最终数值 = 职业基础数值 + 四维影响 + 装备加成
     *
     * 四维影响规则（根据设计文档）：
     * - 力量：影响物理攻击力、物理防御力、（小幅）命中
     * - 敏捷：影响速度、暴击率、暴击伤害、命中、闪避
     * - 法力：影响法力值上限、法术攻击力、法术防御力、（小幅）命中
     * - 体力：影响生命值上限、物理防御力、（小幅）法术防御力
     */
    public void recalculateFinalStats() {
        // 从职业基础属性开始
        resetStatsToBase();

        // 计算总四维（玩家自身 + 装备提供）
        TotalAttributes totalAttrs = calculateTotalAttributes();

        // 应用四维对战斗属性的影响
        applyAttributeEffects(totalAttrs);

        // 应用装备提供的直接战斗属性
        applyEquipmentDirectStats();
    }

    /**
     * 重置属性为基础值
     */
    private void resetStatsToBase() {
        setMaxHealth(getBaseMaxHealth());
        setMaxMana(getBaseMaxMana());
        setPhysicalAttack(getBasePhysicalAttack());
        setPhysicalDefense(getBasePhysicalDefense());
        setMagicAttack(getBaseMagicAttack());
        setMagicDefense(getBaseMagicDefense());
        setSpeed(getBaseSpeed());
        setCritRate(getBaseCritRate());
        setCritDamage(getBaseCritDamage());
        setHitRate(getBaseHitRate());
        setDodgeRate(getBaseDodgeRate());
    }

    /**
     * 计算总四维（玩家自身 + 装备提供）
     */
    private TotalAttributes calculateTotalAttributes() {
        int totalStrength = strength;
        int totalAgility = agility;
        int totalIntelligence = intelligence;
        int totalVitality = vitality;

        if (equipment != null) {
            for (Equipment eq : equipment.values()) {
                if (eq != null) {
                    totalStrength += eq.getStrength();
                    totalAgility += eq.getAgility();
                    totalIntelligence += eq.getIntelligence();
                    totalVitality += eq.getVitality();
                }
            }
        }

        return new TotalAttributes(totalStrength, totalAgility, totalIntelligence, totalVitality);
    }

    /**
     * 应用四维对战斗属性的影响
     */
    private void applyAttributeEffects(TotalAttributes attrs) {
        // 力量影响
        setPhysicalAttack(getPhysicalAttack() + attrs.strength * GameConstants.STRENGTH_TO_PHYSICAL_ATTACK);
        setPhysicalDefense(getPhysicalDefense() + attrs.strength * GameConstants.STRENGTH_TO_PHYSICAL_DEFENSE);
        setHitRate(getHitRate() + attrs.strength * GameConstants.STRENGTH_TO_HIT_RATE);

        // 敏捷影响
        setSpeed(getSpeed() + attrs.agility * GameConstants.AGILITY_TO_SPEED);
        setCritRate(getCritRate() + attrs.agility * GameConstants.AGILITY_TO_CRIT_RATE);
        setCritDamage(getCritDamage() + attrs.agility * GameConstants.AGILITY_TO_CRIT_DAMAGE);
        setHitRate(getHitRate() + attrs.agility * GameConstants.AGILITY_TO_HIT_RATE);
        setDodgeRate(getDodgeRate() + attrs.agility * GameConstants.AGILITY_TO_DODGE_RATE);

        // 智力影响
        setMaxMana(getMaxMana() + attrs.intelligence * GameConstants.INTELLIGENCE_TO_MAX_MANA);
        setMagicAttack(getMagicAttack() + attrs.intelligence * GameConstants.INTELLIGENCE_TO_MAGIC_ATTACK);
        setMagicDefense(getMagicDefense() + attrs.intelligence * GameConstants.INTELLIGENCE_TO_MAGIC_DEFENSE);
        setHitRate(getHitRate() + attrs.intelligence * GameConstants.INTELLIGENCE_TO_HIT_RATE);

        // 体力影响
        setMaxHealth(getMaxHealth() + attrs.vitality * GameConstants.VITALITY_TO_MAX_HEALTH);
        setPhysicalDefense(getPhysicalDefense() + attrs.vitality * GameConstants.VITALITY_TO_PHYSICAL_DEFENSE);
        setMagicDefense(getMagicDefense() + (int)(attrs.vitality * GameConstants.VITALITY_TO_MAGIC_DEFENSE));
    }

    /**
     * 应用装备提供的直接战斗属性
     */
    private void applyEquipmentDirectStats() {
        if (equipment == null) {
            return;
        }

        for (Equipment eq : equipment.values()) {
            if (eq != null) {
                setPhysicalAttack(getPhysicalAttack() + eq.getPhysicalAttack());
                setPhysicalDefense(getPhysicalDefense() + eq.getPhysicalDefense());
                setMagicAttack(getMagicAttack() + eq.getMagicAttack());
                setMagicDefense(getMagicDefense() + eq.getMagicDefense());
                setSpeed(getSpeed() + eq.getSpeed());
                setCritRate(getCritRate() + eq.getCritRate());
                setCritDamage(getCritDamage() + eq.getCritDamage());
                setHitRate(getHitRate() + eq.getHitRate());
                setDodgeRate(getDodgeRate() + eq.getDodgeRate());
            }
        }
    }

    /**
     * 总四维属性的辅助类
     */
    private static class TotalAttributes {
        final int strength;
        final int agility;
        final int intelligence;
        final int vitality;

        TotalAttributes(int strength, int agility, int intelligence, int vitality) {
            this.strength = strength;
            this.agility = agility;
            this.intelligence = intelligence;
            this.vitality = vitality;
        }
    }

    /**
     * 获取玩家阵营
     * 根据设计文档：玩家的阵营由队伍决定
     */
    @Override
    public String getFaction() {
        // 如果有队伍，阵营从队伍获取（需要在服务层实现）
        // 这里返回存储的阵营值
        return super.getFaction();
    }

    /**
     * 是否可通过
     */
    @Override
    public boolean isPassable() {
        return true;
    }

    @Override
    public String getEntityType() {
        return "PLAYER";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(id, player.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * 背包槽位抽象类
     * 使用多态设计替代类型标记模式
     */
    public abstract static class InventorySlot {
        /**
         * 获取槽位中的物品ID
         */
        public abstract String getItemId();

        /**
         * 获取槽位中的物品名称
         */
        public abstract String getItemName();

        /**
         * 获取数量
         */
        public abstract int getQuantity();

        /**
         * 设置数量
         */
        public abstract void setQuantity(int quantity);

        /**
         * 判断是否为普通物品槽
         */
        public boolean isItem() {
            return this instanceof ItemSlot;
        }

        /**
         * 判断是否为装备槽
         */
        public boolean isEquipment() {
            return this instanceof EquipmentSlot;
        }

        /**
         * 获取普通物品（如果是ItemSlot）
         */
        public Item getItem() {
            return isItem() ? ((ItemSlot) this).getItemObject() : null;
        }

        /**
         * 获取装备（如果是EquipmentSlot）
         */
        public Equipment getEquipment() {
            return isEquipment() ? ((EquipmentSlot) this).getEquipmentObject() : null;
        }

        /**
         * 静态工厂方法：创建物品槽
         */
        public static InventorySlot forItem(Item item, int quantity) {
            return new ItemSlot(item, quantity);
        }

        /**
         * 静态工厂方法：创建装备槽
         */
        public static InventorySlot forEquipment(Equipment equipment) {
            return new EquipmentSlot(equipment);
        }
    }

    /**
     * 普通物品槽（可堆叠）
     */
    @lombok.Getter
    @lombok.Setter
    public static class ItemSlot extends InventorySlot {
        private Item item;
        private int quantity;

        public ItemSlot() {
        }

        public ItemSlot(Item item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }

        @Override
        public String getItemId() {
            return item != null ? item.getId() : null;
        }

        @Override
        public String getItemName() {
            return item != null ? item.getName() : null;
        }

        /**
         * 获取物品对象（内部使用）
         */
        public Item getItemObject() {
            return item;
        }
    }

    /**
     * 装备槽（不可堆叠，有实例编号）
     */
    @lombok.Getter
    @lombok.Setter
    public static class EquipmentSlot extends InventorySlot {
        private Equipment equipment;

        public EquipmentSlot() {
        }

        public EquipmentSlot(Equipment equipment) {
            this.equipment = equipment;
        }

        @Override
        public String getItemId() {
            return equipment != null ? equipment.getId() : null;
        }

        @Override
        public String getItemName() {
            return equipment != null ? equipment.getName() : null;
        }

        @Override
        public int getQuantity() {
            return 1; // 装备固定为1
        }

        @Override
        public void setQuantity(int quantity) {
            // 装备不支持修改数量，忽略此操作
        }

        /**
         * 获取装备对象（内部使用）
         */
        public Equipment getEquipmentObject() {
            return equipment;
        }
    }
}
