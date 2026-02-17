package com.heibai.clawworld.interfaces.command.impl.party;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PartyAcceptInviteCommand extends Command {
    private String inviterName;

    @Builder
    public PartyAcceptInviteCommand(String inviterName, String rawCommand) {
        this.inviterName = inviterName;
        setRawCommand(rawCommand);
        setType(CommandType.PARTY_ACCEPT_INVITE);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        throw new UnsupportedOperationException("需要注入 PartyService 来执行此指令");
    }

    @Override
    public ValidationResult validate() {
        if (inviterName == null || inviterName.trim().isEmpty()) {
            return ValidationResult.error("邀请者名称不能为空");
        }
        return ValidationResult.success();
    }
}
