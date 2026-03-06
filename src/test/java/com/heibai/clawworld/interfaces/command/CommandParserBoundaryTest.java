package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.map.MoveCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 指令解析器边界条件测试
 */
@DisplayName("指令解析器边界条件测试")
class CommandParserBoundaryTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    @DisplayName("空指令")
    void testParseEmptyCommand() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("", CommandContext.WindowType.MAP);
        });
    }

    @Test
    @DisplayName("null指令")
    void testParseNullCommand() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse(null, CommandContext.WindowType.MAP);
        });
    }

    @Test
    @DisplayName("只有空格的指令")
    void testParseWhitespaceCommand() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("   ", CommandContext.WindowType.MAP);
        });
    }

    @Test
    @DisplayName("未知指令")
    void testParseUnknownCommand() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("unknown command", CommandContext.WindowType.MAP);
        });
    }

    @Test
    @DisplayName("大小写不敏感")
    void testParseCaseInsensitive() throws CommandParser.CommandParseException {
        Command command1 = parser.parse("MOVE 5 10", CommandContext.WindowType.MAP);
        Command command2 = parser.parse("Move 5 10", CommandContext.WindowType.MAP);
        Command command3 = parser.parse("move 5 10", CommandContext.WindowType.MAP);

        assertTrue(command1 instanceof MoveCommand);
        assertTrue(command2 instanceof MoveCommand);
        assertTrue(command3 instanceof MoveCommand);
    }

    @Test
    @DisplayName("多余空格处理")
    void testParseExtraWhitespace() throws CommandParser.CommandParseException {
        Command command = parser.parse("  move   5   10  ", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof MoveCommand);

        MoveCommand moveCommand = (MoveCommand) command;
        assertEquals(5, moveCommand.getTargetX());
        assertEquals(10, moveCommand.getTargetY());
    }
}
