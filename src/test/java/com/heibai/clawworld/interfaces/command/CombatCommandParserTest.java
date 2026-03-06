package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.combat.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 战斗窗口指令解析器测试
 */
@DisplayName("战斗窗口指令解析器测试")
class CombatCommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    @DisplayName("解析释放技能指令 - 非指向")
    void testParseCastCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("cast 群体治疗", CommandContext.WindowType.COMBAT);

        assertNotNull(command);
        assertTrue(command instanceof CastCommand);

        CastCommand castCommand = (CastCommand) command;
        assertEquals("群体治疗", castCommand.getSkillName());
    }

    @Test
    @DisplayName("解析释放技能指令 - 指向")
    void testParseCastTargetCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("cast 火球术 哥布林", CommandContext.WindowType.COMBAT);

        assertNotNull(command);
        assertTrue(command instanceof CastTargetCommand);

        CastTargetCommand castCommand = (CastTargetCommand) command;
        assertEquals("火球术", castCommand.getSkillName());
        assertEquals("哥布林", castCommand.getTargetName());
    }

    @Test
    @DisplayName("解析战斗中使用物品指令")
    void testParseUseItemCombatCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("use 生命药剂", CommandContext.WindowType.COMBAT);

        assertNotNull(command);
        assertTrue(command instanceof UseItemCombatCommand);

        UseItemCombatCommand useCommand = (UseItemCombatCommand) command;
        assertEquals("生命药剂", useCommand.getItemName());
    }

    @Test
    @DisplayName("解析战斗等待指令")
    void testParseWaitCombatCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("wait", CommandContext.WindowType.COMBAT);

        assertNotNull(command);
        assertTrue(command instanceof WaitCombatCommand);
        assertEquals(Command.CommandType.WAIT_COMBAT, command.getType());
    }

    @Test
    @DisplayName("解析退出战斗指令")
    void testParseEndCombatCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("end", CommandContext.WindowType.COMBAT);

        assertNotNull(command);
        assertTrue(command instanceof EndCombatCommand);
        assertEquals(Command.CommandType.END_COMBAT, command.getType());
    }
}
