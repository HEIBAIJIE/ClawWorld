package com.heibai.clawworld.interfaces.command.impl;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TradeRejectRequestCommand extends Command {
    private String requesterName;

    @Builder
    public TradeRejectRequestCommand(String requesterName, String rawCommand) {
        this.requesterName = requesterName;
        setRawCommand(rawCommand);
        setType(CommandType.TRADE_REJECT_REQUEST);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        throw new UnsupportedOperationException("需要注入 TradeService 来执行此指令");
    }

    @Override
    public ValidationResult validate() {
        if (requesterName == null || requesterName.trim().isEmpty()) {
            return ValidationResult.error("请求者名称不能为空");
        }
        return ValidationResult.success();
    }
}
