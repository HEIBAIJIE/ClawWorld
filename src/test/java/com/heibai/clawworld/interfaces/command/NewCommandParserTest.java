package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 新增命令解析测试
 */
class NewCommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    // ==================== 组队命令测试 ====================

    @Test
    void testParsePartyInviteCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party invite 张三", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_INVITE, command.getType());
        assertTrue(command instanceof PartyInviteCommand);
        assertEquals("张三", ((PartyInviteCommand) command).getPlayerName());
    }

    @Test
    void testParsePartyAcceptInviteCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party accept 李四", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_ACCEPT_INVITE, command.getType());
        assertTrue(command instanceof PartyAcceptInviteCommand);
        assertEquals("李四", ((PartyAcceptInviteCommand) command).getInviterName());
    }

    @Test
    void testParsePartyRejectInviteCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party reject 王五", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_REJECT_INVITE, command.getType());
        assertTrue(command instanceof PartyRejectInviteCommand);
        assertEquals("王五", ((PartyRejectInviteCommand) command).getInviterName());
    }

    @Test
    void testParsePartyRequestJoinCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party request 赵六", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_REQUEST_JOIN, command.getType());
        assertTrue(command instanceof PartyRequestJoinCommand);
        assertEquals("赵六", ((PartyRequestJoinCommand) command).getPlayerName());
    }

    @Test
    void testParsePartyAcceptRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party acceptrequest 孙七", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_ACCEPT_REQUEST, command.getType());
        assertTrue(command instanceof PartyAcceptRequestCommand);
        assertEquals("孙七", ((PartyAcceptRequestCommand) command).getRequesterName());
    }

    @Test
    void testParsePartyRejectRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party rejectrequest 周八", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_REJECT_REQUEST, command.getType());
        assertTrue(command instanceof PartyRejectRequestCommand);
        assertEquals("周八", ((PartyRejectRequestCommand) command).getRequesterName());
    }

    // ==================== 交易命令测试 ====================

    @Test
    void testParseTradeRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade request 吴九", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.TRADE_REQUEST, command.getType());
        assertTrue(command instanceof TradeRequestCommand);
        assertEquals("吴九", ((TradeRequestCommand) command).getPlayerName());
    }

    @Test
    void testParseTradeAcceptRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade accept 郑十", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.TRADE_ACCEPT_REQUEST, command.getType());
        assertTrue(command instanceof TradeAcceptRequestCommand);
        assertEquals("郑十", ((TradeAcceptRequestCommand) command).getRequesterName());
    }

    @Test
    void testParseTradeRejectRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade reject 冯十一", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.TRADE_REJECT_REQUEST, command.getType());
        assertTrue(command instanceof TradeRejectRequestCommand);
        assertEquals("冯十一", ((TradeRejectRequestCommand) command).getRequesterName());
    }

    // ==================== 商店命令测试 ====================

    @Test
    void testParseShopBuyCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("shop buy 生命药剂 5", CommandContext.WindowType.SHOP);

        assertNotNull(command);
        assertEquals(Command.CommandType.SHOP_BUY, command.getType());
        assertTrue(command instanceof ShopBuyCommand);
        assertEquals("生命药剂", ((ShopBuyCommand) command).getItemName());
        assertEquals(5, ((ShopBuyCommand) command).getQuantity());
    }

    @Test
    void testParseShopSellCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("shop sell 铁剑 1", CommandContext.WindowType.SHOP);

        assertNotNull(command);
        assertEquals(Command.CommandType.SHOP_SELL, command.getType());
        assertTrue(command instanceof ShopSellCommand);
        assertEquals("铁剑", ((ShopSellCommand) command).getItemName());
        assertEquals(1, ((ShopSellCommand) command).getQuantity());
    }

    @Test
    void testParseShopLeaveCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("shop leave", CommandContext.WindowType.SHOP);

        assertNotNull(command);
        assertEquals(Command.CommandType.SHOP_LEAVE, command.getType());
        assertTrue(command instanceof ShopLeaveCommand);
    }

    // ==================== 错误情况测试 ====================

    @Test
    void testParsePartyInviteWithoutPlayerName() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("party invite", CommandContext.WindowType.MAP);
        });
    }

    @Test
    void testParseShopBuyWithoutQuantity() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("shop buy 生命药剂", CommandContext.WindowType.SHOP);
        });
    }

    @Test
    void testParseShopBuyWithInvalidQuantity() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("shop buy 生命药剂 abc", CommandContext.WindowType.SHOP);
        });
    }
}
