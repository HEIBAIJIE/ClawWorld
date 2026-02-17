package com.heibai.clawworld.interfaces.command;

import com.heibai.clawworld.application.service.*;
import com.heibai.clawworld.interfaces.command.impl.combat.CastCommand;
import com.heibai.clawworld.interfaces.command.impl.combat.CastTargetCommand;
import com.heibai.clawworld.interfaces.command.impl.combat.UseItemCombatCommand;
import com.heibai.clawworld.interfaces.command.impl.map.*;
import com.heibai.clawworld.interfaces.command.impl.party.*;
import com.heibai.clawworld.interfaces.command.impl.player.AttributeAddCommand;
import com.heibai.clawworld.interfaces.command.impl.player.EquipCommand;
import com.heibai.clawworld.interfaces.command.impl.shop.ShopBuyCommand;
import com.heibai.clawworld.interfaces.command.impl.shop.ShopSellCommand;
import com.heibai.clawworld.interfaces.command.impl.trade.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 指令执行器
 * 负责将解析后的指令分发到相应的服务执行
 */
@Component
@RequiredArgsConstructor
public class CommandExecutor {

    private final PlayerSessionService playerSessionService;
    private final MapEntityService mapEntityService;
    private final PartyService partyService;
    private final CombatService combatService;
    private final TradeService tradeService;
    private final ChatService chatService;
    private final ShopService shopService;

    /**
     * 执行指令
     * @param command 指令对象
     * @param context 执行上下文
     * @return 执行结果
     */
    public CommandResult execute(Command command, CommandContext context) {
        // 先验证指令
        Command.ValidationResult validation = command.validate();
        if (!validation.isValid()) {
            return CommandResult.error(validation.getErrorMessage());
        }

        // 根据指令类型分发到不同的处理方法
        try {
            switch (command.getType()) {
                // 注册窗口指令
                case REGISTER:
                    return executeRegister((RegisterCommand) command, context);

                // 地图窗口指令
                case INSPECT_SELF:
                    return executeInspectSelf(context);
                case INSPECT_CHARACTER:
                    return executeInspectCharacter((InspectCharacterCommand) command, context);
                case SAY:
                    return executeSay((SayCommand) command, context);
                case SAY_TO:
                    return executeSayTo((SayToCommand) command, context);
                case INTERACT:
                    return executeInteract((InteractCommand) command, context);
                case MOVE:
                    return executeMove((MoveCommand) command, context);
                case USE_ITEM:
                    return executeUseItem((UseItemCommand) command, context);
                case EQUIP:
                    return executeEquip((EquipCommand) command, context);
                case ATTRIBUTE_ADD:
                    return executeAttributeAdd((AttributeAddCommand) command, context);
                case PARTY_INVITE:
                    return executePartyInvite((PartyInviteCommand) command, context);
                case PARTY_ACCEPT_INVITE:
                    return executePartyAcceptInvite((PartyAcceptInviteCommand) command, context);
                case PARTY_REJECT_INVITE:
                    return executePartyRejectInvite((PartyRejectInviteCommand) command, context);
                case PARTY_REQUEST_JOIN:
                    return executePartyRequestJoin((PartyRequestJoinCommand) command, context);
                case PARTY_ACCEPT_REQUEST:
                    return executePartyAcceptRequest((PartyAcceptRequestCommand) command, context);
                case PARTY_REJECT_REQUEST:
                    return executePartyRejectRequest((PartyRejectRequestCommand) command, context);
                case PARTY_KICK:
                    return executePartyKick((PartyKickCommand) command, context);
                case PARTY_END:
                    return executePartyEnd(context);
                case PARTY_LEAVE:
                    return executePartyLeave(context);
                case TRADE_REQUEST:
                    return executeTradeRequest((TradeRequestCommand) command, context);
                case TRADE_ACCEPT_REQUEST:
                    return executeTradeAcceptRequest((TradeAcceptRequestCommand) command, context);
                case TRADE_REJECT_REQUEST:
                    return executeTradeRejectRequest((TradeRejectRequestCommand) command, context);
                case WAIT:
                    return executeWait((WaitCommand) command, context);
                case LEAVE:
                    return executeLeave(context);

                // 战斗窗口指令
                case CAST:
                    return executeCast((CastCommand) command, context);
                case CAST_TARGET:
                    return executeCastTarget((CastTargetCommand) command, context);
                case USE_ITEM_COMBAT:
                    return executeUseItemCombat((UseItemCombatCommand) command, context);
                case WAIT_COMBAT:
                    return executeWaitCombat(context);
                case END_COMBAT:
                    return executeEndCombat(context);

                // 交易窗口指令
                case TRADE_ADD:
                    return executeTradeAdd((TradeAddCommand) command, context);
                case TRADE_REMOVE:
                    return executeTradeRemove((TradeRemoveCommand) command, context);
                case TRADE_MONEY:
                    return executeTradeMoney((TradeMoneyCommand) command, context);
                case TRADE_LOCK:
                    return executeTradeLock(context);
                case TRADE_UNLOCK:
                    return executeTradeUnlock(context);
                case TRADE_CONFIRM:
                    return executeTradeConfirm(context);
                case TRADE_END:
                    return executeTradeEnd(context);

                // 商店窗口指令
                case SHOP_BUY:
                    return executeShopBuy((ShopBuyCommand) command, context);
                case SHOP_SELL:
                    return executeShopSell((ShopSellCommand) command, context);
                case SHOP_LEAVE:
                    return executeShopLeave(context);

                default:
                    return CommandResult.error("未知的指令类型: " + command.getType());
            }
        } catch (Exception e) {
            return CommandResult.error("执行指令时发生错误: " + e.getMessage());
        }
    }

    // ==================== 注册窗口指令 ====================

    private CommandResult executeRegister(RegisterCommand command, CommandContext context) {
        PlayerSessionService.SessionResult result = playerSessionService.registerPlayer(
                context.getSessionId(),
                command.getRoleName(),
                command.getPlayerName()
        );

        if (result.isSuccess()) {
            return CommandResult.successWithWindowChange(
                    result.getMessage(),
                    CommandContext.WindowType.MAP,
                    result.getWindowContent()
            );
        } else {
            return CommandResult.error(result.getMessage());
        }
    }

    // ==================== 地图窗口指令 ====================

    private CommandResult executeInspectSelf(CommandContext context) {
        // 调用玩家会话服务获取自身状态
        String detailedStatus = playerSessionService.getPlayerDetailedStatus(context.getPlayerId());
        return CommandResult.success(detailedStatus);
    }

    private CommandResult executeInspectCharacter(InspectCharacterCommand command, CommandContext context) {
        MapEntityService.EntityInfo info = mapEntityService.inspectCharacter(
                context.getPlayerId(),
                command.getCharacterName()
        );

        if (info.isSuccess()) {
            // attributes已经是格式化好的字符串
            return CommandResult.success(info.getAttributes().toString());
        } else {
            return CommandResult.error(info.getMessage());
        }
    }

    private CommandResult executeSay(SayCommand command, CommandContext context) {
        ChatService.ChatResult result;
        switch (command.getChannel()) {
            case "world":
                result = chatService.sendWorldMessage(context.getPlayerId(), command.getMessage());
                break;
            case "map":
                result = chatService.sendMapMessage(context.getPlayerId(), command.getMessage());
                break;
            case "party":
                result = chatService.sendPartyMessage(context.getPlayerId(), command.getMessage());
                break;
            default:
                return CommandResult.error("未知的频道: " + command.getChannel());
        }

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeSayTo(SayToCommand command, CommandContext context) {
        ChatService.ChatResult result = chatService.sendPrivateMessage(
                context.getPlayerId(),
                command.getTargetPlayer(),
                command.getMessage()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeInteract(InteractCommand command, CommandContext context) {
        MapEntityService.InteractionResult result = mapEntityService.interact(
                context.getPlayerId(),
                command.getTargetName(),
                command.getOption()
        );

        if (result.isSuccess()) {
            if (result.isWindowChanged()) {
                return CommandResult.successWithWindowChange(
                        result.getMessage(),
                        CommandContext.WindowType.valueOf(result.getNewWindowType()),
                        "窗口已切换: " + result.getNewWindowType()
                );
            } else {
                return CommandResult.success(result.getMessage());
            }
        } else {
            return CommandResult.error(result.getMessage());
        }
    }

    private CommandResult executeMove(MoveCommand command, CommandContext context) {
        MapEntityService.MoveResult result = mapEntityService.movePlayer(
                context.getPlayerId(),
                command.getTargetX(),
                command.getTargetY()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeUseItem(UseItemCommand command, CommandContext context) {
        PlayerSessionService.OperationResult result = playerSessionService.useItem(
                context.getPlayerId(),
                command.getItemName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeEquip(EquipCommand command, CommandContext context) {
        PlayerSessionService.OperationResult result = playerSessionService.equipItem(
                context.getPlayerId(),
                command.getItemName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeAttributeAdd(AttributeAddCommand command, CommandContext context) {
        PlayerSessionService.OperationResult result = playerSessionService.addAttribute(
                context.getPlayerId(),
                command.getAttributeType(),
                command.getAmount()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyInvite(PartyInviteCommand command, CommandContext context) {
        // 需要先通过玩家名称查找玩家ID
        // 这里简化处理，假设playerName就是playerId
        // 实际应该通过PlayerRepository查找
        PartyService.PartyResult result = partyService.invitePlayer(
                context.getPlayerId(),
                command.getPlayerName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyAcceptInvite(PartyAcceptInviteCommand command, CommandContext context) {
        PartyService.PartyResult result = partyService.acceptInvite(
                context.getPlayerId(),
                command.getInviterName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyRejectInvite(PartyRejectInviteCommand command, CommandContext context) {
        PartyService.PartyResult result = partyService.rejectInvite(
                context.getPlayerId(),
                command.getInviterName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyRequestJoin(PartyRequestJoinCommand command, CommandContext context) {
        PartyService.PartyResult result = partyService.requestJoin(
                context.getPlayerId(),
                command.getPlayerName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyAcceptRequest(PartyAcceptRequestCommand command, CommandContext context) {
        PartyService.PartyResult result = partyService.acceptJoinRequest(
                context.getPlayerId(),
                command.getRequesterName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyRejectRequest(PartyRejectRequestCommand command, CommandContext context) {
        PartyService.PartyResult result = partyService.rejectJoinRequest(
                context.getPlayerId(),
                command.getRequesterName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeRequest(TradeRequestCommand command, CommandContext context) {
        TradeService.TradeResult result = tradeService.requestTrade(
                context.getPlayerId(),
                command.getPlayerName()
        );

        if (result.isSuccess() && result.getWindowId() != null) {
            return CommandResult.successWithWindowChange(
                    result.getMessage(),
                    CommandContext.WindowType.TRADE,
                    result.getWindowId()
            );
        }

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeAcceptRequest(TradeAcceptRequestCommand command, CommandContext context) {
        TradeService.TradeResult result = tradeService.acceptTradeRequest(
                context.getPlayerId(),
                command.getRequesterName()
        );

        if (result.isSuccess() && result.getWindowId() != null) {
            return CommandResult.successWithWindowChange(
                    result.getMessage(),
                    CommandContext.WindowType.TRADE,
                    result.getWindowId()
            );
        }

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeRejectRequest(TradeRejectRequestCommand command, CommandContext context) {
        TradeService.OperationResult result = tradeService.rejectTradeRequest(
                context.getPlayerId(),
                command.getRequesterName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyKick(PartyKickCommand command, CommandContext context) {
        PartyService.PartyResult result = partyService.kickPlayer(
                context.getPlayerId(),
                command.getPlayerName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyEnd(CommandContext context) {
        PartyService.PartyResult result = partyService.disbandParty(context.getPlayerId());

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executePartyLeave(CommandContext context) {
        PartyService.PartyResult result = partyService.leaveParty(context.getPlayerId());

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeWait(WaitCommand command, CommandContext context) {
        PlayerSessionService.OperationResult result = playerSessionService.wait(
                context.getPlayerId(),
                command.getSeconds()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeLeave(CommandContext context) {
        PlayerSessionService.OperationResult result = playerSessionService.logout(context.getSessionId());

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    // ==================== 战斗窗口指令 ====================

    private CommandResult executeCast(CastCommand command, CommandContext context) {
        // 从context中获取combatId（需要在context中添加）
        String combatId = context.getWindowId(); // 假设windowId就是combatId

        CombatService.ActionResult result = combatService.castSkill(
                combatId,
                context.getPlayerId(),
                command.getSkillName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeCastTarget(CastTargetCommand command, CommandContext context) {
        String combatId = context.getWindowId();

        CombatService.ActionResult result = combatService.castSkillOnTarget(
                combatId,
                context.getPlayerId(),
                command.getSkillName(),
                command.getTargetName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeUseItemCombat(UseItemCombatCommand command, CommandContext context) {
        String combatId = context.getWindowId();

        CombatService.ActionResult result = combatService.useItem(
                combatId,
                context.getPlayerId(),
                command.getItemName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeWaitCombat(CommandContext context) {
        String combatId = context.getWindowId();

        CombatService.ActionResult result = combatService.waitTurn(
                combatId,
                context.getPlayerId()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeEndCombat(CommandContext context) {
        String combatId = context.getWindowId();

        CombatService.ActionResult result = combatService.forfeit(
                combatId,
                context.getPlayerId()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    // ==================== 交易窗口指令 ====================

    private CommandResult executeTradeAdd(TradeAddCommand command, CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.addItem(
                tradeId,
                context.getPlayerId(),
                command.getItemName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeRemove(TradeRemoveCommand command, CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.removeItem(
                tradeId,
                context.getPlayerId(),
                command.getItemName()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeMoney(TradeMoneyCommand command, CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.setMoney(
                tradeId,
                context.getPlayerId(),
                command.getAmount()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeLock(CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.lockTrade(
                tradeId,
                context.getPlayerId()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeUnlock(CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.unlockTrade(
                tradeId,
                context.getPlayerId()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeTradeConfirm(CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.confirmTrade(
                tradeId,
                context.getPlayerId()
        );

        if (result.isSuccess()) {
            if (result.isTradeCompleted()) {
                // 交易完成，返回地图窗口
                return CommandResult.successWithWindowChange(
                        result.getMessage(),
                        CommandContext.WindowType.MAP,
                        "交易已完成，返回地图"
                );
            } else {
                return CommandResult.success(result.getMessage());
            }
        } else {
            return CommandResult.error(result.getMessage());
        }
    }

    private CommandResult executeTradeEnd(CommandContext context) {
        String tradeId = context.getWindowId();

        TradeService.OperationResult result = tradeService.cancelTrade(
                tradeId,
                context.getPlayerId()
        );

        if (result.isSuccess()) {
            // 交易取消，返回地图窗口
            return CommandResult.successWithWindowChange(
                    result.getMessage(),
                    CommandContext.WindowType.MAP,
                    "交易已取消，返回地图"
            );
        } else {
            return CommandResult.error(result.getMessage());
        }
    }

    // ==================== 商店窗口指令 ====================

    private CommandResult executeShopBuy(ShopBuyCommand command, CommandContext context) {
        String shopId = context.getWindowId().replace("shop_", "");
        ShopService.OperationResult result = shopService.buyItem(
                context.getPlayerId(),
                shopId,
                command.getItemName(),
                command.getQuantity()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeShopSell(ShopSellCommand command, CommandContext context) {
        String shopId = context.getWindowId().replace("shop_", "");
        ShopService.OperationResult result = shopService.sellItem(
                context.getPlayerId(),
                shopId,
                command.getItemName(),
                command.getQuantity()
        );

        return result.isSuccess() ?
                CommandResult.success(result.getMessage()) :
                CommandResult.error(result.getMessage());
    }

    private CommandResult executeShopLeave(CommandContext context) {
        // 离开商店，返回地图窗口
        return CommandResult.successWithWindowChange(
                "离开商店",
                CommandContext.WindowType.MAP,
                "已离开商店"
        );
    }
}
