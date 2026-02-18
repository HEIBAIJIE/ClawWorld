package com.heibai.clawworld.interfaces.command.impl.map;

import com.heibai.clawworld.application.service.PlayerSessionService;
import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import com.heibai.clawworld.interfaces.command.CommandServiceLocator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UseItemCommand extends Command {
    private String itemName;

    @Builder
    public UseItemCommand(String itemName, String rawCommand) {
        this.itemName = itemName;
        setRawCommand(rawCommand);
        setType(CommandType.USE_ITEM);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        PlayerSessionService.OperationResult result = CommandServiceLocator.getInstance().getPlayerSessionService()
                .useItem(context.getPlayerId(), itemName);

        if (result.isSuccess()) {
            // 如果有背包更新数据，附加到消息中
            String message = result.getMessage();
            if (result.getData() != null) {
                message = message + "\n" + result.getData().toString();
            }
            return CommandResult.success(message);
        } else {
            return CommandResult.error(result.getMessage());
        }
    }

    @Override
    public ValidationResult validate() {
        if (itemName == null || itemName.trim().isEmpty()) {
            return ValidationResult.error("物品名称不能为空");
        }
        return ValidationResult.success();
    }
}
