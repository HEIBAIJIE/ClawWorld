package com.heibai.clawworld.interfaces.command.impl;

import com.heibai.clawworld.interfaces.command.Command;
import com.heibai.clawworld.interfaces.command.CommandContext;
import com.heibai.clawworld.interfaces.command.CommandResult;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EndCombatCommand extends Command {

    @Builder
    public EndCombatCommand(String rawCommand) {
        setRawCommand(rawCommand);
        setType(CommandType.END_COMBAT);
    }

    @Override
    public CommandResult execute(CommandContext context) {
        throw new UnsupportedOperationException("需要注入 CombatService 来执行此指令");
    }

    @Override
    public ValidationResult validate() {
        return ValidationResult.success();
    }
}
