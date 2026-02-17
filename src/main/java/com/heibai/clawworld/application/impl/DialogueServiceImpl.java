package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.DialogueService;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.character.NpcConfig;
import com.heibai.clawworld.infrastructure.config.data.map.MapEntityConfig;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 对话服务实现
 */
@Service
@RequiredArgsConstructor
public class DialogueServiceImpl implements DialogueService {

    private final PlayerRepository playerRepository;
    private final ConfigDataManager configDataManager;

    @Override
    public DialogueResult talk(String playerId, String npcName) {
        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return DialogueResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();

        // 查找 NPC
        NpcConfig npcConfig = findNpcByName(player.getCurrentMapId(), npcName);
        if (npcConfig == null) {
            return DialogueResult.error("找不到 NPC: " + npcName);
        }

        // 检查 NPC 是否支持对话
        if (!npcConfig.isHasDialogue()) {
            return DialogueResult.error(npcName + " 不想和你说话");
        }

        // 检查玩家是否在 NPC 附近
        MapEntityConfig entityConfig = findNpcEntityConfig(player.getCurrentMapId(), npcConfig.getId());
        if (entityConfig != null) {
            int dx = Math.abs(player.getX() - entityConfig.getX());
            int dy = Math.abs(player.getY() - entityConfig.getY());
            if (dx > 1 || dy > 1) {
                return DialogueResult.error("你离 " + npcName + " 太远了，请先靠近");
            }
        }

        // 获取对话内容
        String dialoguesStr = npcConfig.getDialogues();
        if (dialoguesStr == null || dialoguesStr.isEmpty()) {
            return DialogueResult.error(npcName + " 没有什么想说的");
        }

        // 解析对话内容（使用 | 分隔多行对话）
        List<String> dialogueLines = new ArrayList<>();
        for (String line : dialoguesStr.split("\\|")) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                dialogueLines.add(trimmed);
            }
        }

        if (dialogueLines.isEmpty()) {
            return DialogueResult.error(npcName + " 没有什么想说的");
        }

        // 构建对话消息
        StringBuilder message = new StringBuilder();
        message.append("与 ").append(npcName).append(" 对话：\n\n");
        for (String line : dialogueLines) {
            message.append(npcName).append(": \"").append(line).append("\"\n");
        }

        return DialogueResult.success(message.toString(), dialogueLines);
    }

    /**
     * 根据名称查找地图上的 NPC 配置
     */
    private NpcConfig findNpcByName(String mapId, String npcName) {
        // 遍历地图上的所有 NPC 实体配置
        List<MapEntityConfig> mapEntities = configDataManager.getMapEntities(mapId);
        for (MapEntityConfig entity : mapEntities) {
            if ("NPC".equals(entity.getEntityType())) {
                NpcConfig npcConfig = configDataManager.getNpc(entity.getEntityId());
                if (npcConfig != null && npcConfig.getName().equals(npcName)) {
                    return npcConfig;
                }
            }
        }
        return null;
    }

    /**
     * 查找 NPC 在地图上的位置配置
     */
    private MapEntityConfig findNpcEntityConfig(String mapId, String npcId) {
        List<MapEntityConfig> mapEntities = configDataManager.getMapEntities(mapId);
        for (MapEntityConfig entity : mapEntities) {
            if ("NPC".equals(entity.getEntityType()) && npcId.equals(entity.getEntityId())) {
                return entity;
            }
        }
        return null;
    }
}
