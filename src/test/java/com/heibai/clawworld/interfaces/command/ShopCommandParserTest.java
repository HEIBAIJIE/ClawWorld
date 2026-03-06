package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.shop.ShopBuyCommand;
import com.heibai.clawworld.interfaces.command.impl.shop.ShopLeaveCommand;
import com.heibai.clawworld.interfaces.command.impl.shop.ShopSellCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 商店窗口指令解析器测试
 */
@DisplayName("商店窗口指令解析器测试")
class ShopCommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    @Test
    @DisplayName("解析商店购买指令")
    void testParseShopBuyCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("shop buy 生命药剂 5", CommandContext.WindowType.SHOP);

        assertNotNull(command);
        assertEquals(Command.CommandType.SHOP_BUY, command.getType());
        assertTrue(command instanceof ShopBuyCommand);
        assertEquals("生命药剂", ((ShopBuyCommand) command).getItemName());
        assertEquals(5, ((ShopBuyCommand) command).getQuantity());
    }

    @Test
    @DisplayName("解析商店出售指令")
    void testParseShopSellCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("shop sell 铁剑 1", CommandContext.WindowType.SHOP);

        assertNotNull(command);
        assertEquals(Command.CommandType.SHOP_SELL, command.getType());
        assertTrue(command instanceof ShopSellCommand);
        assertEquals("铁剑", ((ShopSellCommand) command).getItemName());
        assertEquals(1, ((ShopSellCommand) command).getQuantity());
    }

    @Test
    @DisplayName("解析离开商店指令")
    void testParseShopLeaveCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("shop leave", CommandContext.WindowType.SHOP);

        assertNotNull(command);
        assertEquals(Command.CommandType.SHOP_LEAVE, command.getType());
        assertTrue(command instanceof ShopLeaveCommand);
    }

    @Test
    @DisplayName("解析商店购买指令 - 缺少数量")
    void testParseShopBuyWithoutQuantity() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("shop buy 生命药剂", CommandContext.WindowType.SHOP);
        });
    }

    @Test
    @DisplayName("解析商店购买指令 - 数量格式错误")
    void testParseShopBuyWithInvalidQuantity() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("shop buy 生命药剂 abc", CommandContext.WindowType.SHOP);
        });
    }
}
