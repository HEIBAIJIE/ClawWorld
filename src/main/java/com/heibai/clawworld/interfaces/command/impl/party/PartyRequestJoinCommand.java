package com.heibai.clawworld.interfaces.command.impl.party;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartyRequestJoinCommand extends Command {
    private String playerName;

    @Builder
    public PartyRequestJoinCommand(String playerName, String rawCommand) {
        this.playerName = playerName;
        setRawCommand(rawCommand);
        setType(CommandType.PARTY_REQUEST_JOIN);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        throw new UnsupportedOperationException("需要注入 PartyService 来执行此指令");
    }

    @Override
    public ValidationResult validate() {
        if (playerName == null || playerName.trim().isEmpty()) {
            return ValidationResult.error("玩家名称不能为空");
        }
        return ValidationResult.success();
    }
}
