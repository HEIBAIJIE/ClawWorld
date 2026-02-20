package com.heibai.clawworld.interfaces.command.impl.trade;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 交易窗口等待指令
 * 用于在交易过程中等待对方操作，同时刷新交易状态
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class TradeWaitCommand extends Command {
    private int seconds;

    @Builder
    public TradeWaitCommand(int seconds, String rawCommand) {
        this.seconds = seconds;
        setRawCommand(rawCommand);
        setType(CommandType.WAIT);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        // 交易窗口的 wait 指令只是等待一段时间
        // 实际的等待由服务端的固定时延机制处理
        // 这里直接返回成功，让服务端返回最新的交易状态
        if (seconds > 0) {
            try {
                Thread.sleep(Math.min(seconds, 10) * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return CommandResult.success("等待完成");
    }

    @Override
    public ValidationResult validate() {
        if (seconds < 0 || seconds > 60) {
            return ValidationResult.error("等待时间必须在0-60秒之间");
        }
        return ValidationResult.success();
    }
}
