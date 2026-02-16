package com.heibai.clawworld.application.service.window;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PlayerMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 商店窗口内容生成器
 */
@Component("shopWindowContentGenerator")
@RequiredArgsConstructor
public class ShopWindowContentGenerator implements WindowContentGenerator {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    @Override
    public String generateContent(WindowContext context) {
        StringBuilder sb = new StringBuilder();
        String shopId = context.getWindowId().replace("shop_", "");
        sb.append("=== 商店 ===\n");
        sb.append("商店主: ").append(shopId).append("\n\n");

        sb.append("--- 商品列表 ---\n");
        sb.append("（商店商品列表需要从NPC配置中读取）\n");
        sb.append("示例商品：\n");
        sb.append("- 生命药剂 x10 (价格: 50金币)\n");
        sb.append("- 法力药剂 x10 (价格: 50金币)\n");
        sb.append("- 铁剑 x1 (价格: 200金币)\n");
        sb.append("\n");

        Optional<PlayerEntity> playerOpt = playerRepository.findById(context.getPlayerId());
        if (playerOpt.isPresent()) {
            PlayerEntity player = playerOpt.get();
            sb.append("--- 你的状态 ---\n");
            sb.append(String.format("金币: %d\n", player.getGold()));
            sb.append("\n");

            sb.append("--- 你的背包 ---\n");
            Player domainPlayer = playerMapper.toDomain(player);
            if (domainPlayer.getInventory() != null && !domainPlayer.getInventory().isEmpty()) {
                for (Player.InventorySlot slot : domainPlayer.getInventory()) {
                    if (slot.isItem()) {
                        sb.append(String.format("%s x%d\n", slot.getItem().getName(), slot.getQuantity()));
                    } else if (slot.isEquipment()) {
                        sb.append(String.format("%s\n", slot.getEquipment().getDisplayName()));
                    }
                }
            } else {
                sb.append("背包为空\n");
            }
            sb.append("\n");
        }

        sb.append("--- 可用指令 ---\n");
        sb.append("shop buy [物品名] [数量] - 购买商品\n");
        sb.append("shop sell [物品名] [数量] - 出售物品\n");
        sb.append("shop leave - 离开商店\n");

        return sb.toString();
    }
}
