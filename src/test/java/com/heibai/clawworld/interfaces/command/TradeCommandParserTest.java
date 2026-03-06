package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.trade.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 交易窗口指令解析器测试
 */
@DisplayName("交易窗口指令解析器测试")
class TradeCommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    @DisplayName("解析添加交易物品指令")
    void testParseTradeAddCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade add 铁剑#1", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeAddCommand);

        TradeAddCommand addCommand = (TradeAddCommand) command;
        assertEquals("铁剑#1", addCommand.getItemName());
    }

    @Test
    @DisplayName("解析移除交易物品指令")
    void testParseTradeRemoveCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade remove 铁剑#1", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeRemoveCommand);

        TradeRemoveCommand removeCommand = (TradeRemoveCommand) command;
        assertEquals("铁剑#1", removeCommand.getItemName());
    }

    @Test
    @DisplayName("解析设置交易金额指令")
    void testParseTradeMoneyCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade money 1 234 567", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeMoneyCommand);

        TradeMoneyCommand moneyCommand = (TradeMoneyCommand) command;
        assertEquals(1, moneyCommand.getGold());
        assertEquals(234, moneyCommand.getSilver());
        assertEquals(567, moneyCommand.getCopper());
    }

    @Test
    @DisplayName("解析锁定交易指令")
    void testParseTradeLockCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade lock", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeLockCommand);
        assertEquals(Command.CommandType.TRADE_LOCK, command.getType());
    }

    @Test
    @DisplayName("解析解锁交易指令")
    void testParseTradeUnlockCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade unlock", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeUnlockCommand);
        assertEquals(Command.CommandType.TRADE_UNLOCK, command.getType());
    }

    @Test
    @DisplayName("解析确认交易指令")
    void testParseTradeConfirmCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade confirm", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeConfirmCommand);
        assertEquals(Command.CommandType.TRADE_CONFIRM, command.getType());
    }

    @Test
    @DisplayName("解析取消交易指令")
    void testParseTradeEndCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade end", CommandContext.WindowType.TRADE);

        assertNotNull(command);
        assertTrue(command instanceof TradeEndCommand);
        assertEquals(Command.CommandType.TRADE_END, command.getType());
    }
}
