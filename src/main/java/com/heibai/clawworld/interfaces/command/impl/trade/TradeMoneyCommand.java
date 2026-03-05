package com.heibai.clawworld.interfaces.command.impl.trade;

import com.heibai.clawworld.application.service.TradeService;
import com.heibai.clawworld.domain.util.CurrencyFormatter;
import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import com.heibai.clawworld.interfaces.command.CommandServiceLocator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TradeMoneyCommand extends Command {
    private int gold;
    private int silver;
    private int copper;

    @Builder
    public TradeMoneyCommand(int gold, int silver, int copper, String rawCommand) {
        this.gold = gold;
        this.silver = silver;
        this.copper = copper;
        setRawCommand(rawCommand);
        setType(CommandType.TRADE_MONEY);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String tradeId = context.getWindowId();

        // 将金/银/铜转换为铜币总数
        int totalCopper = CurrencyFormatter.toCopper(gold, silver, copper);

        TradeService.OperationResult result = CommandServiceLocator.getInstance().getTradeService()
                .setMoney(tradeId, context.getPlayerId(), totalCopper);

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    @Override
    public ValidationResult validate() {
        if (gold < 0 || silver < 0 || copper < 0) {
            return ValidationResult.error("金额不能为负数");
        }
        if (silver >= 1000) {
            return ValidationResult.error("银币数量不能超过999");
        }
        if (copper >= 1000) {
            return ValidationResult.error("铜币数量不能超过999");
        }
        return ValidationResult.success();
    }
}
