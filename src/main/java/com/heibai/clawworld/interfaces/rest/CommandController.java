package com.heibai.clawworld.interfaces.rest;

import com.heibai.clawworld.interfaces.command.*;
import com.heibai.clawworld.interfaces.dto.CommandRequest;
import com.heibai.clawworld.interfaces.dto.CommandResponse;
import com.heibai.clawworld.infrastructure.persistence.entity.AccountEntity;
import com.heibai.clawworld.application.impl.AuthService;
import com.heibai.clawworld.interfaces.log.UnifiedResponseGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 指令控制器
 * 处理玩家发送的指令
 */
@RestController
@RequestMapping("/api/command")
@RequiredArgsConstructor
public class CommandController {

    private final CommandParser commandParser;
    private final CommandExecutor commandExecutor;
    private final AuthService authService;
    private final UnifiedResponseGenerator responseGenerator;

    /**
     * 执行指令
     * 支持复合指令：用分号分隔多条指令，最多10条，按顺序依次执行，结果统一返回
     * 示例：interact 篝火 休息;move 3 3
     * @param request 指令请求（包含sessionId和command）
     * @return 指令执行结果
     */
    @PostMapping("/execute")
    public ResponseEntity<CommandResponse> executeCommand(@RequestBody CommandRequest request) {

        // 验证会话
        Optional<AccountEntity> account = authService.getAccountBySessionId(request.getSessionId());
        if (account.isEmpty() || !account.get().isOnline()) {
            return ResponseEntity.status(401)
                    .body(CommandResponse.error("会话无效或已过期"));
        }

        AccountEntity accountEntity = account.get();

        // 记录指令和时间戳
        accountEntity.setLastCommand(request.getCommand());
        accountEntity.setLastCommandTimestamp(System.currentTimeMillis());
        // 保存指令记录
        authService.saveAccount(accountEntity);

        // 拆分复合指令（分号分隔，最多10条）
        String[] subCommands = request.getCommand().split(";", -1);
        if (subCommands.length > 10) {
            subCommands = java.util.Arrays.copyOf(subCommands, 10);
        }

        StringBuilder combinedResponse = new StringBuilder();
        boolean anySuccess = false;
        boolean lastSuccess = true;

        for (int i = 0; i < subCommands.length; i++) {
            String subCommandStr = subCommands[i].trim();
            if (subCommandStr.isEmpty()) {
                continue;
            }

            // 每条子指令执行前重新获取最新账号状态（窗口可能因上一条指令改变）
            accountEntity = authService.getAccountBySessionId(request.getSessionId()).orElse(accountEntity);

            String windowId = accountEntity.getCurrentWindowId();
            CommandContext.WindowType windowType = accountEntity.getCurrentWindowType() != null ?
                    CommandContext.WindowType.valueOf(accountEntity.getCurrentWindowType()) : null;

            try {
                // 解析指令
                Command command = commandParser.parse(subCommandStr, windowType);

                // 构建执行上下文
                CommandContext context = CommandContext.builder()
                        .sessionId(request.getSessionId())
                        .windowId(windowId)
                        .playerId(accountEntity.getPlayerId())
                        .windowType(windowType)
                        .build();

                // 执行指令
                CommandResult result = commandExecutor.execute(command, context);

                // 如果窗口改变，更新账号的窗口状态
                if (result.isWindowChanged()) {
                    authService.updateWindowState(
                            request.getSessionId(),
                            result.getWindowContent(),
                            result.getNewWindowType() != null ? result.getNewWindowType().name() : null
                    );
                }

                // 重新获取账号信息，以确保获取最新的playerId（特别是注册场景）
                Optional<AccountEntity> updatedAccount = authService.getAccountBySessionId(request.getSessionId());
                String playerId = updatedAccount.isPresent() ? updatedAccount.get().getPlayerId() : accountEntity.getPlayerId();

                // 生成统一的日志格式响应
                String responseText = responseGenerator.generateResponse(
                    playerId,
                    subCommandStr,
                    result.getMessage(),
                    windowType,
                    result.getNewWindowType(),
                    result.isInventoryChanged()
                );

                if (combinedResponse.length() > 0) {
                    combinedResponse.append("\n");
                }
                combinedResponse.append(responseText);
                lastSuccess = result.isSuccess();
                if (result.isSuccess()) {
                    anySuccess = true;
                }

                // 遇到失败指令，终止后续执行
                if (!result.isSuccess()) {
                    break;
                }

            } catch (CommandParser.CommandParseException e) {
                String playerId = getLatestPlayerId(request.getSessionId(), accountEntity);
                CommandContext.WindowType windowType2 = accountEntity.getCurrentWindowType() != null ?
                        CommandContext.WindowType.valueOf(accountEntity.getCurrentWindowType()) : null;
                String errorResponse = responseGenerator.generateErrorResponse(
                    playerId,
                    "指令解析失败: " + e.getMessage(),
                    windowType2
                );
                if (combinedResponse.length() > 0) {
                    combinedResponse.append("\n");
                }
                combinedResponse.append(errorResponse);
                lastSuccess = false;
                break;
            } catch (Exception e) {
                String playerId = getLatestPlayerId(request.getSessionId(), accountEntity);
                CommandContext.WindowType windowType2 = accountEntity.getCurrentWindowType() != null ?
                        CommandContext.WindowType.valueOf(accountEntity.getCurrentWindowType()) : null;
                String errorResponse = responseGenerator.generateErrorResponse(
                    playerId,
                    "服务器内部错误: " + e.getMessage(),
                    windowType2
                );
                if (combinedResponse.length() > 0) {
                    combinedResponse.append("\n");
                }
                combinedResponse.append(errorResponse);
                lastSuccess = false;
                break;
            }
        }

        String finalResponse = combinedResponse.toString();
        if (lastSuccess) {
            return ResponseEntity.ok(CommandResponse.success(finalResponse));
        } else {
            return ResponseEntity.badRequest().body(CommandResponse.error(finalResponse));
        }
    }

    private String getLatestPlayerId(String sessionId, AccountEntity fallback) {
        return authService.getAccountBySessionId(sessionId)
                .map(AccountEntity::getPlayerId)
                .orElse(fallback.getPlayerId());
    }
}
