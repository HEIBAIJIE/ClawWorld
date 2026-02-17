package com.heibai.clawworld.interfaces.command.impl.map;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import com.heibai.clawworld.interfaces.command.CommandServiceLocator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 查看物品详情指令
 * inspect [物品名称]
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InspectItemCommand extends Command {
    private String itemName;

    @Builder
    public InspectItemCommand(String itemName, String rawCommand) {
        this.itemName = itemName;
        setRawCommand(rawCommand);
        setType(CommandType.INSPECT_ITEM);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        String itemInfo = CommandServiceLocator.getInstance().getCharacterInfoService()
                .generateItemInfo(context.getPlayerId(), itemName);
        if (itemInfo == null) {
            return CommandResult.error("物品不存在: " + itemName);
        }
        return CommandResult.success(itemInfo);
    }

    @Override
    public ValidationResult validate() {
        if (itemName == null || itemName.trim().isEmpty()) {
            return ValidationResult.error("物品名称不能为空");
        }
        return ValidationResult.success();
    }
}
