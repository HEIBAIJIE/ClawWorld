package com.heibai.clawworld.application.impl.window;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PlayerMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 交易窗口内容生成器
 */
@Component("tradeWindowContentGenerator")
@RequiredArgsConstructor
public class TradeWindowContentGenerator implements WindowContentGenerator {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;

    @Override
    public String generateContent(WindowContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 交易 ===\n");
        sb.append("交易ID: ").append(context.getWindowId()).append("\n\n");

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

        sb.append("--- 交易信息 ---\n");
        sb.append("（交易详细信息需要从交易系统获取）\n\n");

        sb.append("--- 可用指令 ---\n");
        sb.append("trade add [物品名] - 添加物品到交易\n");
        sb.append("trade remove [物品名] - 从交易移除物品\n");
        sb.append("trade money [金额] - 设置交易金额\n");
        sb.append("trade lock - 锁定交易\n");
        sb.append("trade unlock - 解锁交易\n");
        sb.append("trade confirm - 确认交易\n");
        sb.append("trade end - 取消交易\n");

        return sb.toString();
    }
}
