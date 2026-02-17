package com.heibai.clawworld.application.impl.window;

import com.heibai.clawworld.application.service.PartyService;
import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.character.Party;
import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.domain.item.Equipment;
import com.heibai.clawworld.domain.map.GameMap;
import com.heibai.clawworld.domain.map.MapEntity;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.RoleConfig;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.entity.TradeEntity;
import com.heibai.clawworld.infrastructure.persistence.mapper.PlayerMapper;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import com.heibai.clawworld.infrastructure.persistence.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 地图窗口内容生成器
 */
@Component("mapWindowContentGenerator")
@RequiredArgsConstructor
public class MapWindowContentGenerator implements WindowContentGenerator {

    private final ConfigDataManager configDataManager;
    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final PartyService partyService;
    private final TradeRepository tradeRepository;

    @Override
    public String generateContent(WindowContext context) {
        Player player = context.getPlayer();
        GameMap map = context.getMap();
        List<ChatMessage> chatHistory = context.getChatHistory();

        StringBuilder sb = new StringBuilder();

        // 标题
        sb.append("=== ").append(map.getName()).append(" ===\n");
        sb.append(map.getDescription()).append("\n");
        if (!map.isSafe()) {
            sb.append("【危险区域】推荐等级: ").append(map.getRecommendedLevel()).append("\n");
        } else {
            sb.append("【安全区域】\n");
        }
        sb.append("\n");

        // 获取地图上的所有实体
        List<MapEntity> allEntities = new ArrayList<>();
        if (map.getEntities() != null) {
            allEntities.addAll(map.getEntities());
        }
        // 添加当前地图上的所有玩家
        List<PlayerEntity> playersOnMap = playerRepository.findAll().stream()
                .filter(p -> p.getCurrentMapId() != null && p.getCurrentMapId().equals(map.getId()))
                .collect(Collectors.toList());
        for (PlayerEntity p : playersOnMap) {
            Player domainPlayer = playerMapper.toDomain(p);
            allEntities.add(domainPlayer);
        }

        // 地图网格
        sb.append("--- 地图 ---\n");
        if (map.getTerrain() != null && !map.getTerrain().isEmpty()) {
            for (int y = map.getHeight() - 1; y >= 0; y--) {
                for (int x = 0; x < map.getWidth(); x++) {
                    sb.append(String.format("(%d,%d) ", x, y));

                    MapEntity entityAtPos = null;
                    for (MapEntity entity : allEntities) {
                        if (entity.getX() == x && entity.getY() == y) {
                            if (entityAtPos == null || "PLAYER".equals(entity.getEntityType())) {
                                entityAtPos = entity;
                            }
                        }
                    }

                    if (entityAtPos != null) {
                        sb.append(entityAtPos.getName());
                    } else if (y < map.getTerrain().size() && x < map.getTerrain().get(y).size()) {
                        GameMap.TerrainCell cell = map.getTerrain().get(y).get(x);
                        if (cell.getTerrainTypes() != null && !cell.getTerrainTypes().isEmpty()) {
                            sb.append(cell.getTerrainTypes().get(0));
                        } else {
                            sb.append("空地");
                        }
                    } else {
                        sb.append("空地");
                    }

                    sb.append("  ");
                }
                sb.append("\n");
            }
        } else {
            sb.append("地图数据加载中...\n");
        }
        sb.append("\n");

        // 玩家状态
        appendPlayerStatus(sb, player);

        // 技能
        appendSkills(sb, player);

        // 装备
        appendEquipment(sb, player);

        // 地图实体列表
        appendMapEntities(sb, player, allEntities, map);

        // 背包
        appendInventory(sb, player);

        // 组队情况
        appendPartyInfo(sb, player);

        // 可达目标
        appendReachableTargets(sb, player, allEntities);

        // 聊天记录
        appendChatHistory(sb, chatHistory);

        // 可用指令
        appendAvailableCommands(sb);

        return sb.toString();
    }

    private void appendPlayerStatus(StringBuilder sb, Player player) {
        sb.append("--- 你的状态 ---\n");
        sb.append(String.format("角色: %s (%s) Lv.%d\n",
            player.getName() != null ? player.getName() : "未命名",
            getRoleName(player.getRoleId()),
            player.getLevel()));
        sb.append(String.format("位置: (%d, %d)\n", player.getX(), player.getY()));
        sb.append(String.format("经验: %d  金币: %d\n", player.getExperience(), player.getGold()));
        sb.append(String.format("力量%d 敏捷%d 智力%d 体力%d\n",
            player.getStrength(), player.getAgility(),
            player.getIntelligence(), player.getVitality()));
        sb.append(String.format("生命%d/%d 法力%d/%d\n",
            player.getCurrentHealth(), player.getMaxHealth(),
            player.getCurrentMana(), player.getMaxMana()));
        sb.append(String.format("物攻%d 物防%d 法攻%d 法防%d 速度%d\n",
            player.getPhysicalAttack(), player.getPhysicalDefense(),
            player.getMagicAttack(), player.getMagicDefense(), player.getSpeed()));
        if (player.getFreeAttributePoints() > 0) {
            sb.append(String.format("可用属性点: %d\n", player.getFreeAttributePoints()));
        }
        sb.append("\n");
    }

    private void appendSkills(StringBuilder sb, Player player) {
        sb.append("--- 技能 ---\n");
        if (player.getSkills() != null && !player.getSkills().isEmpty()) {
            for (String skillId : player.getSkills()) {
                String skillName = getSkillName(skillId);
                sb.append(skillName).append("\n");
            }
        } else {
            sb.append("无技能\n");
        }
        sb.append("\n");
    }

    private void appendEquipment(StringBuilder sb, Player player) {
        sb.append("--- 装备 ---\n");
        if (player.getEquipment() != null && !player.getEquipment().isEmpty()) {
            for (Map.Entry<Equipment.EquipmentSlot, Equipment> entry : player.getEquipment().entrySet()) {
                sb.append(String.format("%s: %s\n",
                    getSlotName(entry.getKey()),
                    entry.getValue().getDisplayName()));
            }
        } else {
            sb.append("无装备\n");
        }
        sb.append("\n");
    }

    private void appendMapEntities(StringBuilder sb, Player player, List<MapEntity> allEntities, GameMap map) {
        sb.append("--- 地图实体 ---\n");
        if (!allEntities.isEmpty()) {
            for (MapEntity entity : allEntities) {
                if (entity.getName().equals(player.getName())) {
                    continue;
                }

                sb.append(String.format("%s (%d,%d)", entity.getName(), entity.getX(), entity.getY()));

                int distance = Math.abs(entity.getX() - player.getX()) + Math.abs(entity.getY() - player.getY());
                if (distance <= 1) {
                    sb.append(" [可直接交互]");
                } else {
                    sb.append(" [需移动]");
                }

                if (entity.getEntityType() != null) {
                    sb.append(" [类型：").append(entity.getEntityType()).append("]");
                }

                if (entity.isInteractable()) {
                    List<String> options = getEntityInteractionOptions(entity, player, map);
                    if (options != null && !options.isEmpty()) {
                        sb.append(" [交互选项: ");
                        sb.append(String.join(", ", options));
                        sb.append("]");
                    }
                }

                sb.append("\n");
            }
        } else {
            sb.append("地图上没有其他实体\n");
        }
        sb.append("\n");
    }

    private void appendInventory(StringBuilder sb, Player player) {
        sb.append("--- 背包 ---\n");
        if (player.getInventory() != null && !player.getInventory().isEmpty()) {
            for (Player.InventorySlot slot : player.getInventory()) {
                if (slot.isItem()) {
                    sb.append(String.format("%s x%d\n", slot.getItem().getName(), slot.getQuantity()));
                } else if (slot.isEquipment()) {
                    sb.append(String.format("%s\n", slot.getEquipment().getDisplayName()));
                }
            }
        } else {
            sb.append("背包为空\n");
        }
        sb.append("\n");
    }

    private void appendPartyInfo(StringBuilder sb, Player player) {
        sb.append("--- 组队情况 ---\n");
        if (player.getPartyId() != null) {
            if (player.isPartyLeader()) {
                sb.append("你是队长\n");
            } else {
                sb.append("你在队伍中\n");
            }
            sb.append("队伍ID: ").append(player.getPartyId()).append("\n");
        } else {
            sb.append("你当前没有队伍\n");
        }
        sb.append("\n");
    }

    private void appendReachableTargets(StringBuilder sb, Player player, List<MapEntity> allEntities) {
        sb.append("--- 移动后可达目标 ---\n");
        boolean hasReachableTarget = false;
        for (MapEntity entity : allEntities) {
            if (entity.getName().equals(player.getName())) {
                continue;
            }

            int dx = Math.abs(entity.getX() - player.getX());
            int dy = Math.abs(entity.getY() - player.getY());
            if ((dx > 1 || dy > 1) && entity.isInteractable()) {
                sb.append(String.format("- %s: 移动到 (%d,%d) 可交互\n",
                    entity.getName(), entity.getX(), entity.getY()));
                hasReachableTarget = true;
            }
        }
        if (!hasReachableTarget) {
            sb.append("没有需要移动才能到达的目标\n");
        }
        sb.append("\n");
    }

    private void appendChatHistory(StringBuilder sb, List<ChatMessage> chatHistory) {
        sb.append("--- 最近聊天 ---\n");
        if (chatHistory != null && !chatHistory.isEmpty()) {
            int count = 0;
            for (ChatMessage msg : chatHistory) {
                if (count >= 10) break;
                String channelPrefix = "";
                switch (msg.getChannelType()) {
                    case WORLD: channelPrefix = "[世界]"; break;
                    case MAP: channelPrefix = "[地图]"; break;
                    case PARTY: channelPrefix = "[队伍]"; break;
                    case PRIVATE: channelPrefix = "[私聊]"; break;
                }
                sb.append(String.format("%s %s: %s\n", channelPrefix, msg.getSenderNickname(), msg.getMessage()));
                count++;
            }
        } else {
            sb.append("(暂无聊天记录，使用 say 指令发送消息)\n");
        }
        sb.append("\n");
    }

    private void appendAvailableCommands(StringBuilder sb) {
        sb.append("--- 可用指令 ---\n");
        sb.append("move [x] [y] - 移动到指定位置\n");
        sb.append("inspect self - 查看自身详细状态\n");
        sb.append("inspect [角色名] - 查看其他角色\n");
        sb.append("interact [目标名] [选项] - 与实体交互\n");
        sb.append("say [频道] [消息] - 聊天 (频道: world/map/party)\n");
        sb.append("use [物品名] - 使用物品\n");
        sb.append("equip [装备名] - 装备物品\n");
        sb.append("attribute add [str/agi/int/vit] [数量] - 加属性点\n");
        sb.append("wait [秒数] - 等待\n");
        sb.append("leave - 下线\n");
    }

    private List<String> getEntityInteractionOptions(MapEntity entity, Player viewer, GameMap map) {
        List<String> options = new ArrayList<>(entity.getInteractionOptions(viewer.getFaction(), map.isSafe()));

        if ("PLAYER".equals(entity.getEntityType()) && entity instanceof Player) {
            Player targetPlayer = (Player) entity;
            addPlayerSpecificOptions(options, viewer, targetPlayer);
        }

        return options;
    }

    private void addPlayerSpecificOptions(List<String> options, Player viewer, Player target) {
        Party viewerParty = partyService.getPlayerParty(viewer.getId());
        Party targetParty = partyService.getPlayerParty(target.getId());

        if (targetParty == null || targetParty.isSolo()) {
            options.add("邀请组队");
        }

        if (targetParty != null && targetParty.getPendingInvitations() != null) {
            boolean hasInvitation = targetParty.getPendingInvitations().stream()
                    .anyMatch(inv -> inv.getInviterId().equals(target.getId())
                            && inv.getInviteeId().equals(viewer.getId())
                            && !inv.isExpired());
            if (hasInvitation) {
                options.add("接受组队邀请");
                options.add("拒绝组队邀请");
            }
        }

        if (targetParty != null && !targetParty.isSolo()) {
            options.add("请求加入队伍");
        }

        if (viewerParty != null && viewerParty.isLeader(viewer.getId()) && viewerParty.getPendingRequests() != null) {
            boolean hasRequest = viewerParty.getPendingRequests().stream()
                    .anyMatch(req -> req.getRequesterId().equals(target.getId()) && !req.isExpired());
            if (hasRequest) {
                options.add("接受组队请求");
                options.add("拒绝组队请求");
            }
        }

        List<TradeEntity> activeTrades = tradeRepository.findActiveTradesByPlayerId(
                TradeEntity.TradeStatus.ACTIVE, viewer.getId());
        List<TradeEntity> pendingTrades = tradeRepository.findActiveTradesByPlayerId(
                TradeEntity.TradeStatus.PENDING, viewer.getId());

        if (activeTrades.isEmpty() && pendingTrades.isEmpty()) {
            options.add("请求交易");
        }

        List<TradeEntity> targetPendingTrades = tradeRepository.findByStatusAndReceiverId(
                TradeEntity.TradeStatus.PENDING, viewer.getId());
        boolean hasTradeRequest = targetPendingTrades.stream()
                .anyMatch(t -> t.getInitiatorId().equals(target.getId()));
        if (hasTradeRequest) {
            options.add("接受交易请求");
            options.add("拒绝交易请求");
        }
    }

    private String getSlotName(Equipment.EquipmentSlot slot) {
        switch (slot) {
            case HEAD: return "头部";
            case CHEST: return "上装";
            case LEGS: return "下装";
            case FEET: return "鞋子";
            case LEFT_HAND: return "左手";
            case RIGHT_HAND: return "右手";
            case ACCESSORY1: return "饰品1";
            case ACCESSORY2: return "饰品2";
            default: return slot.name();
        }
    }

    private String getRoleName(String roleId) {
        if (roleId == null) return "未知";
        RoleConfig roleConfig = configDataManager.getRole(roleId);
        return roleConfig != null ? roleConfig.getName() : "未知";
    }

    private String getSkillName(String skillId) {
        if (skillId == null) return "未知技能";
        if ("basic_attack".equals(skillId)) return "普通攻击";
        var skillConfig = configDataManager.getSkill(skillId);
        return skillConfig != null ? skillConfig.getName() : skillId;
    }
}
