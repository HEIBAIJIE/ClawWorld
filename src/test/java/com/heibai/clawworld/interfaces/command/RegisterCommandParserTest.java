package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.map.RegisterCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 注册窗口指令解析器测试
 */
@DisplayName("注册窗口指令解析器测试")
class RegisterCommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    @DisplayName("解析注册指令 - 成功")
    void testParseRegisterCommand_Success() throws CommandParser.CommandParseException {
        Command command = parser.parse("register 战士 张三", CommandContext.WindowType.REGISTER);

        assertNotNull(command);
        assertTrue(command instanceof RegisterCommand);
        assertEquals(Command.CommandType.REGISTER, command.getType());

        RegisterCommand registerCommand = (RegisterCommand) command;
        assertEquals("战士", registerCommand.getRoleName());
        assertEquals("张三", registerCommand.getPlayerName());
    }

    @Test
    @DisplayName("解析注册指令 - 参数不足")
    void testParseRegisterCommand_InsufficientArgs() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("register 战士", CommandContext.WindowType.REGISTER);
        });
    }

    @Test
    @DisplayName("注册窗口不支持其他指令")
    void testRegisterWindow_UnsupportedCommand() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("move 5 10", CommandContext.WindowType.REGISTER);
        });
    }
}
