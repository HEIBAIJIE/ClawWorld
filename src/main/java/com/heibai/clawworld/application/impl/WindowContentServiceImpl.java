package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.WindowContentService;
import com.heibai.clawworld.application.impl.window.WindowContentGenerator;
import com.heibai.clawworld.application.impl.window.WindowContentGeneratorFactory;
import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.domain.map.GameMap;
import com.heibai.clawworld.interfaces.command.CommandContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 窗口内容生成服务实现（重构版）
 * 使用工厂模式和多态设计
 */
@Service
@RequiredArgsConstructor
public class WindowContentServiceImpl implements WindowContentService {

    private final WindowContentGeneratorFactory generatorFactory;

    @Override
    public String generateRegisterWindowContent() {
        WindowContentGenerator generator = generatorFactory.getGenerator(CommandContext.WindowType.REGISTER);
        return generator.generateContent(WindowContentGenerator.WindowContext.builder().build());
    }

    @Override
    public String generateMapWindowContent(Player player, GameMap map) {
        return generateMapWindowContent(player, map, null);
    }

    @Override
    public String generateMapWindowContent(Player player, GameMap map, List<ChatMessage> chatHistory) {
        WindowContentGenerator generator = generatorFactory.getGenerator(CommandContext.WindowType.MAP);
        WindowContentGenerator.WindowContext context = WindowContentGenerator.WindowContext.builder()
                .player(player)
                .map(map)
                .chatHistory(chatHistory)
                .build();
        return generator.generateContent(context);
    }

    @Override
    public String generateCombatWindowContent(String playerId, String combatId) {
        WindowContentGenerator generator = generatorFactory.getGenerator(CommandContext.WindowType.COMBAT);
        WindowContentGenerator.WindowContext context = WindowContentGenerator.WindowContext.builder()
                .playerId(playerId)
                .windowId(combatId)
                .build();
        return generator.generateContent(context);
    }

    @Override
    public String generateTradeWindowContent(String playerId, String tradeId) {
        WindowContentGenerator generator = generatorFactory.getGenerator(CommandContext.WindowType.TRADE);
        WindowContentGenerator.WindowContext context = WindowContentGenerator.WindowContext.builder()
                .playerId(playerId)
                .windowId(tradeId)
                .build();
        return generator.generateContent(context);
    }

    @Override
    public String generateShopWindowContent(String playerId, String shopId) {
        WindowContentGenerator generator = generatorFactory.getGenerator(CommandContext.WindowType.SHOP);
        WindowContentGenerator.WindowContext context = WindowContentGenerator.WindowContext.builder()
                .playerId(playerId)
                .windowId(shopId)
                .build();
        return generator.generateContent(context);
    }
}
