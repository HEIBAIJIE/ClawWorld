package com.heibai.clawworld.interfaces.command.impl;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopBuyCommand extends Command {
    private String itemName;
    private int quantity;

    @Builder
    public ShopBuyCommand(String itemName, int quantity, String rawCommand) {
        this.itemName = itemName;
        this.quantity = quantity;
        setRawCommand(rawCommand);
        setType(CommandType.SHOP_BUY);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        throw new UnsupportedOperationException("需要注入 ShopService 来执行此指令");
    }

    @Override
    public ValidationResult validate() {
        if (itemName == null || itemName.trim().isEmpty()) {
            return ValidationResult.error("物品名称不能为空");
        }
        if (quantity <= 0) {
            return ValidationResult.error("购买数量必须大于0");
        }
        return ValidationResult.success();
    }
}
