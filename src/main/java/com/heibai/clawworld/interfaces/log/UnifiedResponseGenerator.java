package com.heibai.clawworld.interfaces.log;

import com.heibai.clawworld.application.service.ChatService;
import com.heibai.clawworld.application.service.MapEntityService;
import com.heibai.clawworld.application.service.PlayerSessionService;
import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.domain.map.GameMap;
import com.heibai.clawworld.domain.map.MapEntity;
import com.heibai.clawworld.infrastructure.factory.MapInitializationService;
import com.heibai.clawworld.infrastructure.persistence.entity.AccountEntity;
import com.heibai.clawworld.infrastructure.persistence.repository.AccountRepository;
import com.heibai.clawworld.interfaces.command.CommandContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 统一响应生成服务
 * 将指令执行结果转换为统一的日志格式
 */
@Service
@RequiredArgsConstructor
public class UnifiedResponseGenerator {

    private final AccountRepository accountRepository;
    private final PlayerSessionService playerSessionService;
    private final MapEntityService mapEntityService;
    private final ChatService chatService;
    private final MapInitializationService mapInitializationService;
    private final MapWindowLogGenerator mapWindowLogGenerator;
    private final StateLogGenerator stateLogGenerator;
    private final CombatWindowLogGenerator combatWindowLogGenerator;
    private final TradeWindowLogGenerator tradeWindowLogGenerator;
    private final ShopWindowLogGenerator shopWindowLogGenerator;

    /**
     * 生成完整的响应（包含客户端指令日志 + 状态日志 + 可选的窗口日志）
     */
    public String generateResponse(String playerId, String command, String commandResult,
                                   CommandContext.WindowType currentWindowType,
                                   CommandContext.WindowType newWindowType) {
        GameLogBuilder builder = new GameLogBuilder();

        Optional<AccountEntity> accountOpt = accountRepository.findByPlayerId(playerId);
        if (!accountOpt.isPresent()) {
            builder.addState("指令响应", "错误: 无法获取玩家状态");
            return builder.build();
        }
        AccountEntity account = accountOpt.get();

        // 1. 添加客户端指令日志
        if (account.getLastCommand() != null && account.getLastCommandTimestamp() != null) {
            GameLog clientLog = GameLog.builder()
                .source(GameLog.Source.CLIENT)
                .timestamp(account.getLastCommandTimestamp())
                .type(GameLog.Type.COMMAND)
                .subType("发送指令")
                .content(account.getLastCommand())
                .build();
            builder.addLog(clientLog);
        }

        // 2. 根据窗口类型生成状态日志
        if (currentWindowType == CommandContext.WindowType.MAP) {
            // 地图窗口：生成环境变化和指令响应
            stateLogGenerator.generateMapStateLogs(builder, playerId, commandResult);
        } else if (currentWindowType == CommandContext.WindowType.COMBAT) {
            // 战斗窗口：生成战斗状态
            // TODO: 需要从战斗系统获取战斗日志
            builder.addState("指令响应", commandResult + "执行完毕，" + commandResult);
        } else if (currentWindowType == CommandContext.WindowType.TRADE) {
            // 交易窗口：生成交易状态
            // TODO: 需要从交易系统获取交易状态
            builder.addState("指令响应", commandResult + "执行完毕，" + commandResult);
        } else if (currentWindowType == CommandContext.WindowType.SHOP) {
            // 商店窗口：生成商店状态
            // TODO: 需要从商店系统获取商店状态
            builder.addState("指令响应", commandResult + "执行完毕，" + commandResult);
        } else {
            // 其他窗口：只返回指令响应
            builder.addState("指令响应", commandResult);
        }

        // 3. 如果窗口发生变化，添加窗口变化日志和新窗口内容
        if (newWindowType != null && newWindowType != currentWindowType) {
            String windowChangeMsg = String.format("你已经从%s切换到%s",
                getWindowTypeName(currentWindowType),
                getWindowTypeName(newWindowType));
            builder.addState("窗口变化", windowChangeMsg);

            // 生成新窗口的内容
            generateNewWindowContent(builder, playerId, newWindowType);
        }

        return builder.build();
    }

    /**
     * 生成错误响应
     */
    public String generateErrorResponse(String playerId, String errorMessage) {
        GameLogBuilder builder = new GameLogBuilder();

        Optional<AccountEntity> accountOpt = accountRepository.findByPlayerId(playerId);
        if (accountOpt.isPresent()) {
            AccountEntity account = accountOpt.get();
            // 添加客户端指令日志
            if (account.getLastCommand() != null && account.getLastCommandTimestamp() != null) {
                GameLog clientLog = GameLog.builder()
                    .source(GameLog.Source.CLIENT)
                    .timestamp(account.getLastCommandTimestamp())
                    .type(GameLog.Type.COMMAND)
                    .subType("发送指令")
                    .content(account.getLastCommand())
                    .build();
                builder.addLog(clientLog);
            }
        }

        builder.addState("指令响应", "错误: " + errorMessage);
        return builder.build();
    }

    /**
     * 生成新窗口内容
     */
    private void generateNewWindowContent(GameLogBuilder builder, String playerId,
                                          CommandContext.WindowType windowType) {
        Player player = playerSessionService.getPlayerState(playerId);
        if (player == null) {
            return;
        }

        if (windowType == CommandContext.WindowType.MAP) {
            // 生成地图窗口内容
            GameMap map = mapInitializationService.getMap(player.getMapId());
            if (map != null) {
                List<MapEntity> allEntities = new ArrayList<>();
                if (map.getEntities() != null) {
                    allEntities.addAll(map.getEntities());
                }
                // 添加当前地图上的所有玩家
                allEntities.addAll(mapEntityService.getMapEntities(player.getMapId()));

                List<ChatMessage> chatHistory = chatService.getChatHistory(playerId);
                mapWindowLogGenerator.generateMapWindowLogs(builder, player, map, allEntities, chatHistory);
            }
        } else if (windowType == CommandContext.WindowType.TRADE) {
            // 生成交易窗口内容
            // TODO: 需要获取交易对象和对方玩家信息
            builder.addWindow("交易窗口", "交易窗口已打开");
        } else if (windowType == CommandContext.WindowType.COMBAT) {
            // 生成战斗窗口内容
            // TODO: 需要获取战斗对象
            builder.addWindow("战斗窗口", "战斗窗口已打开");
        } else if (windowType == CommandContext.WindowType.SHOP) {
            // 生成商店窗口内容
            // TODO: 需要获取商店对象
            builder.addWindow("商店窗口", "商店窗口已打开");
        }
    }

    /**
     * 获取窗口类型名称
     */
    private String getWindowTypeName(CommandContext.WindowType windowType) {
        if (windowType == null) {
            return "未知窗口";
        }
        return switch (windowType) {
            case MAP -> "地图窗口";
            case COMBAT -> "战斗窗口";
            case TRADE -> "交易窗口";
            case SHOP -> "商店窗口";
            case REGISTER -> "注册窗口";
        };
    }
}
