package com.heibai.clawworld.interfaces.command.impl.shop;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import com.heibai.clawworld.interfaces.command.CommandServiceLocator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShopLeaveCommand extends Command {

    @Builder
    public ShopLeaveCommand(String rawCommand) {
        setRawCommand(rawCommand);
        setType(CommandType.SHOP_LEAVE);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        // 清除玩家的商店状态
        var playerSessionService = CommandServiceLocator.getInstance().getPlayerSessionService();
        Player player = playerSessionService.getPlayerState(context.getPlayerId());
        if (player != null) {
            player.setCurrentShopId(null);
            playerSessionService.savePlayerState(player);
        }

        return CommandResult.successWithWindowChange(
                "离开商店",
                CommandContext.WindowType.MAP,
                "已离开商店"
        );
    }

    @Override
    public ValidationResult validate() {
        return ValidationResult.success();
    }
}
