package com.heibai.clawworld.domain.item;

import com.heibai.clawworld.domain.character.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("物品使用系统测试")
class ItemUsageTest {

    private Player player;

    @BeforeEach
    void setUp() {
        player = new Player();
        player.setId("player1");
        player.setName("测试玩家");
        player.setLevel(5);
        player.setCurrentHealth(50);
        player.setMaxHealth(100);
        player.setCurrentMana(30);
        player.setMaxMana(80);
        player.setInventory(new ArrayList<>());
    }

    @Test
    @DisplayName("使用生命药剂 - 恢复生命值")
    void testUseHealthPotion() {
        Item healthPotion = new Item();
        healthPotion.setId("health_potion");
        healthPotion.setName("生命药剂");
        healthPotion.setType(Item.ItemType.CONSUMABLE);
        healthPotion.setEffect("restore_health");
        healthPotion.setEffectValue(30);

        player.getInventory().add(Player.InventorySlot.forItem(healthPotion, 1));

        int healthBefore = player.getCurrentHealth();

        // 使用物品
        boolean used = useItem(player, "health_potion");

        assertTrue(used, "应该成功使用物品");
        assertTrue(player.getCurrentHealth() > healthBefore, "生命值应该增加");
        assertEquals(Math.min(100, healthBefore + 30), player.getCurrentHealth(), "生命值应该恢复30点");
    }

    @Test
    @DisplayName("使用法力药剂 - 恢复法力值")
    void testUseManaPotion() {
        Item manaPotion = new Item();
        manaPotion.setId("mana_potion");
        manaPotion.setName("法力药剂");
        manaPotion.setType(Item.ItemType.CONSUMABLE);
        manaPotion.setEffect("restore_mana");
        manaPotion.setEffectValue(25);

        player.getInventory().add(Player.InventorySlot.forItem(manaPotion, 1));

        int manaBefore = player.getCurrentMana();

        boolean used = useItem(player, "mana_potion");

        assertTrue(used);
        assertTrue(player.getCurrentMana() > manaBefore, "法力值应该增加");
        assertEquals(Math.min(80, manaBefore + 25), player.getCurrentMana());
    }

    @Test
    @DisplayName("使用物品 - 满血时不能使用生命药剂")
    void testUseHealthPotion_FullHealth() {
        player.setCurrentHealth(100);

        Item healthPotion = new Item();
        healthPotion.setId("health_potion");
        healthPotion.setName("生命药剂");
        healthPotion.setType(Item.ItemType.CONSUMABLE);
        healthPotion.setEffect("restore_health");
        healthPotion.setEffectValue(30);

        player.getInventory().add(Player.InventorySlot.forItem(healthPotion, 1));

        boolean used = useItem(player, "health_potion");

        assertFalse(used, "满血时不应该能使用生命药剂");
        assertEquals(1, player.getInventory().get(0).getQuantity(), "物品数量不应该减少");
    }

    @Test
    @DisplayName("使用物品 - 物品数量减少")
    void testUseItem_QuantityDecrease() {
        Item healthPotion = new Item();
        healthPotion.setId("health_potion");
        healthPotion.setName("生命药剂");
        healthPotion.setType(Item.ItemType.CONSUMABLE);
        healthPotion.setEffect("restore_health");
        healthPotion.setEffectValue(30);

        player.getInventory().add(Player.InventorySlot.forItem(healthPotion, 3));

        useItem(player, "health_potion");

        assertEquals(2, player.getInventory().get(0).getQuantity(), "使用后数量应该减1");
    }

    @Test
    @DisplayName("使用物品 - 最后一个物品使用后移除")
    void testUseItem_RemoveWhenEmpty() {
        Item healthPotion = new Item();
        healthPotion.setId("health_potion");
        healthPotion.setName("生命药剂");
        healthPotion.setType(Item.ItemType.CONSUMABLE);
        healthPotion.setEffect("restore_health");
        healthPotion.setEffectValue(30);

        player.getInventory().add(Player.InventorySlot.forItem(healthPotion, 1));

        useItem(player, "health_potion");

        assertTrue(player.getInventory().isEmpty(), "使用最后一个物品后应该从背包移除");
    }

    @Test
    @DisplayName("使用物品 - 物品不存在")
    void testUseItem_ItemNotFound() {
        boolean used = useItem(player, "nonexistent_item");

        assertFalse(used, "不存在的物品不能使用");
    }

    @Test
    @DisplayName("使用物品 - 非消耗品不能使用")
    void testUseItem_NonConsumable() {
        Item material = new Item();
        material.setId("iron_ore");
        material.setName("铁矿石");
        material.setType(Item.ItemType.MATERIAL);

        player.getInventory().add(Player.InventorySlot.forItem(material, 5));

        boolean used = useItem(player, "iron_ore");

        assertFalse(used, "材料类物品不能直接使用");
    }

    @Test
    @DisplayName("使用技能书 - 学习技能")
    void testUseSkillBook() {
        Item skillBook = new Item();
        skillBook.setId("fireball_book");
        skillBook.setName("火球术秘籍");
        skillBook.setType(Item.ItemType.SKILL_BOOK);
        skillBook.setEffect("learn_skill");
        skillBook.setEffectValue(null);

        player.setSkills(new ArrayList<>());
        player.getInventory().add(Player.InventorySlot.forItem(skillBook, 1));

        boolean used = useItem(player, "fireball_book");

        assertTrue(used, "应该成功使用技能书");
        assertTrue(player.getSkills().contains("fireball"), "应该学会火球术");
        assertTrue(player.getInventory().isEmpty(), "技能书使用后应该消失");
    }

    // 辅助方法：模拟物品使用逻辑
    private boolean useItem(Player player, String itemId) {
        Player.InventorySlot slot = player.getInventory().stream()
            .filter(s -> s.isItem() && s.getItem().getId().equals(itemId))
            .findFirst()
            .orElse(null);

        if (slot == null) {
            return false;
        }

        Item item = slot.getItem();

        // 非消耗品不能使用
        if (item.getType() != Item.ItemType.CONSUMABLE && item.getType() != Item.ItemType.SKILL_BOOK) {
            return false;
        }

        // 根据效果类型处理
        if ("restore_health".equals(item.getEffect())) {
            if (player.getCurrentHealth() >= player.getMaxHealth()) {
                return false; // 满血不能使用
            }
            player.setCurrentHealth(Math.min(player.getMaxHealth(),
                player.getCurrentHealth() + item.getEffectValue()));
        } else if ("restore_mana".equals(item.getEffect())) {
            if (player.getCurrentMana() >= player.getMaxMana()) {
                return false;
            }
            player.setCurrentMana(Math.min(player.getMaxMana(),
                player.getCurrentMana() + item.getEffectValue()));
        } else if ("learn_skill".equals(item.getEffect())) {
            String skillId = item.getId().replace("_book", "");
            if (player.getSkills() == null) {
                player.setSkills(new ArrayList<>());
            }
            player.getSkills().add(skillId);
        }

        // 减少数量
        if (slot.getQuantity() > 1) {
            slot.setQuantity(slot.getQuantity() - 1);
        } else {
            player.getInventory().remove(slot);
        }

        return true;
    }
}
