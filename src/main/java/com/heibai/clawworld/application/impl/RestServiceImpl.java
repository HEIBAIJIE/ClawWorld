package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.RestService;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.map.MapEntityConfig;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 休息服务实现
 */
@Service
@RequiredArgsConstructor
public class RestServiceImpl implements RestService {

    private final PlayerRepository playerRepository;
    private final ConfigDataManager configDataManager;

    @Override
    @Transactional
    public RestResult rest(String playerId, String targetName) {
        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return RestResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();

        // 检查玩家是否在战斗中
        if (player.isInCombat()) {
            return RestResult.error("战斗中无法休息");
        }

        // 查找篝火
        MapEntityConfig campfireConfig = findCampfire(player.getCurrentMapId(), targetName);
        if (campfireConfig == null) {
            return RestResult.error("找不到可以休息的地方: " + targetName);
        }

        // 检查玩家是否在篝火附近（九宫格范围内）
        int dx = Math.abs(player.getX() - campfireConfig.getX());
        int dy = Math.abs(player.getY() - campfireConfig.getY());
        if (dx > 1 || dy > 1) {
            return RestResult.error("你离篝火太远了，请先靠近");
        }

        // 计算恢复量
        int healthBefore = player.getCurrentHealth();
        int manaBefore = player.getCurrentMana();

        // 回满生命和法力
        player.setCurrentHealth(player.getMaxHealth());
        player.setCurrentMana(player.getMaxMana());

        int healthRestored = player.getCurrentHealth() - healthBefore;
        int manaRestored = player.getCurrentMana() - manaBefore;

        playerRepository.save(player);

        // 构建消息
        String message;
        if (healthRestored == 0 && manaRestored == 0) {
            message = "你在篝火旁休息，但你的状态已经是满的";
        } else {
            message = String.format("你在篝火旁休息，生命和法力已完全恢复（生命+%d，法力+%d）",
                    healthRestored, manaRestored);
        }

        return RestResult.success(message, healthRestored, manaRestored);
    }

    /**
     * 查找地图上的篝火
     */
    private MapEntityConfig findCampfire(String mapId, String targetName) {
        List<MapEntityConfig> mapEntities = configDataManager.getMapEntities(mapId);
        for (MapEntityConfig entity : mapEntities) {
            if ("CAMPFIRE".equals(entity.getEntityType())) {
                // 篝火的名称默认为"篝火"
                if ("篝火".equals(targetName) || targetName.equals(entity.getEntityId())) {
                    return entity;
                }
            }
        }
        return null;
    }
}
