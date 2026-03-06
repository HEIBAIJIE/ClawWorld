package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.interfaces.command.impl.map.*;
import com.heibai.clawworld.interfaces.command.impl.party.*;
import com.heibai.clawworld.interfaces.command.impl.trade.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 地图窗口指令解析器测试
 */
@DisplayName("地图窗口指令解析器测试")
class MapCommandParserTest {

    private CommandParser parser;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
    }

    // ==================== 查看指令测试 ====================

    @Test
    @DisplayName("解析查看自身指令")
    void testParseInspectSelfCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("inspect self", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof InspectSelfCommand);
        assertEquals(Command.CommandType.INSPECT_SELF, command.getType());
    }

    @Test
    @DisplayName("解析查看物品指令 - 非self参数应解析为查看物品")
    void testParseInspectCharacterCommand_ShouldThrowException() throws CommandParser.CommandParseException {
        Command command = parser.parse("inspect 小型生命药水", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof InspectItemCommand);
        assertEquals(Command.CommandType.INSPECT_ITEM, command.getType());

        InspectItemCommand inspectCommand = (InspectItemCommand) command;
        assertEquals("小型生命药水", inspectCommand.getItemName());
    }

    // ==================== 聊天指令测试 ====================

    @Test
    @DisplayName("解析公屏聊天指令 - 世界频道")
    void testParseSayCommand_World() throws CommandParser.CommandParseException {
        Command command = parser.parse("say world 大家好", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof SayCommand);

        SayCommand sayCommand = (SayCommand) command;
        assertEquals("world", sayCommand.getChannel());
        assertEquals("大家好", sayCommand.getMessage());
    }

    @Test
    @DisplayName("解析公屏聊天指令 - 地图频道")
    void testParseSayCommand_Map() throws CommandParser.CommandParseException {
        Command command = parser.parse("say map 这里有怪物", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof SayCommand);

        SayCommand sayCommand = (SayCommand) command;
        assertEquals("map", sayCommand.getChannel());
        assertEquals("这里有怪物", sayCommand.getMessage());
    }

    @Test
    @DisplayName("解析公屏聊天指令 - 队伍频道")
    void testParseSayCommand_Party() throws CommandParser.CommandParseException {
        Command command = parser.parse("say party 准备战斗", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof SayCommand);

        SayCommand sayCommand = (SayCommand) command;
        assertEquals("party", sayCommand.getChannel());
        assertEquals("准备战斗", sayCommand.getMessage());
    }

    @Test
    @DisplayName("解析私聊指令")
    void testParseSayToCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("say to 张三 你好吗", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof SayToCommand);

        SayToCommand sayToCommand = (SayToCommand) command;
        assertEquals("张三", sayToCommand.getTargetPlayer());
        assertEquals("你好吗", sayToCommand.getMessage());
    }

    // ==================== 交互和移动指令测试 ====================

    @Test
    @DisplayName("解析交互指令")
    void testParseInteractCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("interact 传送点 传送", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof InteractCommand);

        InteractCommand interactCommand = (InteractCommand) command;
        assertEquals("传送点", interactCommand.getTargetName());
        assertEquals("传送", interactCommand.getOption());
    }

    @Test
    @DisplayName("解析移动指令")
    void testParseMoveCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("move 5 10", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof MoveCommand);

        MoveCommand moveCommand = (MoveCommand) command;
        assertEquals(5, moveCommand.getTargetX());
        assertEquals(10, moveCommand.getTargetY());
    }

    @Test
    @DisplayName("解析移动指令 - 坐标格式错误")
    void testParseMoveCommand_InvalidCoordinates() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("move abc def", CommandContext.WindowType.MAP);
        });
    }

    // ==================== 物品和装备指令测试 ====================

    @Test
    @DisplayName("解析使用物品指令")
    void testParseUseItemCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("use 生命药剂", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof UseItemCommand);

        UseItemCommand useItemCommand = (UseItemCommand) command;
        assertEquals("生命药剂", useItemCommand.getItemName());
    }

    @Test
    @DisplayName("解析使用物品指令 - 带空格的物品名")
    void testParseUseItemCommand_WithSpaces() throws CommandParser.CommandParseException {
        Command command = parser.parse("use 高级生命药剂", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof UseItemCommand);

        UseItemCommand useItemCommand = (UseItemCommand) command;
        assertEquals("高级生命药剂", useItemCommand.getItemName());
    }

    @Test
    @DisplayName("解析装备指令")
    void testParseEquipCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("equip 铁剑#1", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof EquipCommand);

        EquipCommand equipCommand = (EquipCommand) command;
        assertEquals("铁剑#1", equipCommand.getItemName());
    }

    // ==================== 加点指令测试 ====================

    @Test
    @DisplayName("解析加点指令 - 力量")
    void testParseAttributeAddCommand_Strength() throws CommandParser.CommandParseException {
        Command command = parser.parse("attribute add str 5", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof AttributeAddCommand);

        AttributeAddCommand attrCommand = (AttributeAddCommand) command;
        assertEquals("str", attrCommand.getAttributeType());
        assertEquals(5, attrCommand.getAmount());
    }

    @Test
    @DisplayName("解析加点指令 - 敏捷")
    void testParseAttributeAddCommand_Agility() throws CommandParser.CommandParseException {
        Command command = parser.parse("attribute add agi 3", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof AttributeAddCommand);

        AttributeAddCommand attrCommand = (AttributeAddCommand) command;
        assertEquals("agi", attrCommand.getAttributeType());
        assertEquals(3, attrCommand.getAmount());
    }

    @Test
    @DisplayName("解析加点指令 - 智力")
    void testParseAttributeAddCommand_Intelligence() throws CommandParser.CommandParseException {
        Command command = parser.parse("attribute add int 4", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof AttributeAddCommand);

        AttributeAddCommand attrCommand = (AttributeAddCommand) command;
        assertEquals("int", attrCommand.getAttributeType());
        assertEquals(4, attrCommand.getAmount());
    }

    @Test
    @DisplayName("解析加点指令 - 体力")
    void testParseAttributeAddCommand_Vitality() throws CommandParser.CommandParseException {
        Command command = parser.parse("attribute add vit 2", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof AttributeAddCommand);

        AttributeAddCommand attrCommand = (AttributeAddCommand) command;
        assertEquals("vit", attrCommand.getAttributeType());
        assertEquals(2, attrCommand.getAmount());
    }

    // ==================== 组队指令测试 ====================

    @Test
    @DisplayName("解析邀请组队指令")
    void testParsePartyInviteCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party invite 张三", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_INVITE, command.getType());
        assertTrue(command instanceof PartyInviteCommand);
        assertEquals("张三", ((PartyInviteCommand) command).getPlayerName());
    }

    @Test
    @DisplayName("解析接受组队邀请指令")
    void testParsePartyAcceptInviteCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party accept 李四", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_ACCEPT_INVITE, command.getType());
        assertTrue(command instanceof PartyAcceptInviteCommand);
        assertEquals("李四", ((PartyAcceptInviteCommand) command).getInviterName());
    }

    @Test
    @DisplayName("解析拒绝组队邀请指令")
    void testParsePartyRejectInviteCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party reject 王五", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_REJECT_INVITE, command.getType());
        assertTrue(command instanceof PartyRejectInviteCommand);
        assertEquals("王五", ((PartyRejectInviteCommand) command).getInviterName());
    }

    @Test
    @DisplayName("解析请求加入队伍指令")
    void testParsePartyRequestJoinCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party request 赵六", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_REQUEST_JOIN, command.getType());
        assertTrue(command instanceof PartyRequestJoinCommand);
        assertEquals("赵六", ((PartyRequestJoinCommand) command).getPlayerName());
    }

    @Test
    @DisplayName("解析接受组队请求指令")
    void testParsePartyAcceptRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party acceptrequest 孙七", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_ACCEPT_REQUEST, command.getType());
        assertTrue(command instanceof PartyAcceptRequestCommand);
        assertEquals("孙七", ((PartyAcceptRequestCommand) command).getRequesterName());
    }

    @Test
    @DisplayName("解析拒绝组队请求指令")
    void testParsePartyRejectRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party rejectrequest 周八", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.PARTY_REJECT_REQUEST, command.getType());
        assertTrue(command instanceof PartyRejectRequestCommand);
        assertEquals("周八", ((PartyRejectRequestCommand) command).getRequesterName());
    }

    @Test
    @DisplayName("解析踢人指令")
    void testParsePartyKickCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party kick 李四", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof PartyKickCommand);

        PartyKickCommand kickCommand = (PartyKickCommand) command;
        assertEquals("李四", kickCommand.getPlayerName());
    }

    @Test
    @DisplayName("解析解散队伍指令")
    void testParsePartyEndCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party end", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof PartyEndCommand);
        assertEquals(Command.CommandType.PARTY_END, command.getType());
    }

    @Test
    @DisplayName("解析离队指令")
    void testParsePartyLeaveCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("party leave", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof PartyLeaveCommand);
        assertEquals(Command.CommandType.PARTY_LEAVE, command.getType());
    }

    @Test
    @DisplayName("解析邀请组队指令 - 缺少玩家名")
    void testParsePartyInviteWithoutPlayerName() {
        assertThrows(CommandParser.CommandParseException.class, () -> {
            parser.parse("party invite", CommandContext.WindowType.MAP);
        });
    }

    // ==================== 交易请求指令测试 ====================

    @Test
    @DisplayName("解析发起交易请求指令")
    void testParseTradeRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade request 吴九", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.TRADE_REQUEST, command.getType());
        assertTrue(command instanceof TradeRequestCommand);
        assertEquals("吴九", ((TradeRequestCommand) command).getPlayerName());
    }

    @Test
    @DisplayName("解析接受交易请求指令")
    void testParseTradeAcceptRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade accept 郑十", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.TRADE_ACCEPT_REQUEST, command.getType());
        assertTrue(command instanceof TradeAcceptRequestCommand);
        assertEquals("郑十", ((TradeAcceptRequestCommand) command).getRequesterName());
    }

    @Test
    @DisplayName("解析拒绝交易请求指令")
    void testParseTradeRejectRequestCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("trade reject 冯十一", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertEquals(Command.CommandType.TRADE_REJECT_REQUEST, command.getType());
        assertTrue(command instanceof TradeRejectRequestCommand);
        assertEquals("冯十一", ((TradeRejectRequestCommand) command).getRequesterName());
    }

    // ==================== 其他指令测试 ====================

    @Test
    @DisplayName("解析等待指令")
    void testParseWaitCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("wait 10", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof WaitCommand);

        WaitCommand waitCommand = (WaitCommand) command;
        assertEquals(10, waitCommand.getSeconds());
    }

    @Test
    @DisplayName("解析下线指令")
    void testParseLeaveCommand() throws CommandParser.CommandParseException {
        Command command = parser.parse("leave", CommandContext.WindowType.MAP);

        assertNotNull(command);
        assertTrue(command instanceof LeaveCommand);
        assertEquals(Command.CommandType.LEAVE, command.getType());
    }
}
