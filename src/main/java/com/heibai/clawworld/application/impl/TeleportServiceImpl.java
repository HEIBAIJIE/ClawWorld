package com.heibai.clawworld.application.impl;

import com.heibai.clawworld.application.service.TeleportService;
import com.heibai.clawworld.infrastructure.config.ConfigDataManager;
import com.heibai.clawworld.infrastructure.config.data.map.MapConfig;
import com.heibai.clawworld.infrastructure.config.data.map.WaypointConfig;
import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import com.heibai.clawworld.infrastructure.persistence.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 传送服务实现
 */
@Service
@RequiredArgsConstructor
public class TeleportServiceImpl implements TeleportService {

    private final PlayerRepository playerRepository;
    private final ConfigDataManager configDataManager;

    @Override
    @Transactional
    public TeleportResult teleport(String playerId, String waypointName, String targetDisplayName) {
        // 获取玩家信息
        Optional<PlayerEntity> playerOpt = playerRepository.findById(playerId);
        if (!playerOpt.isPresent()) {
            return TeleportResult.error("玩家不存在");
        }

        PlayerEntity player = playerOpt.get();

        // 查找当前传送点
        WaypointConfig currentWaypoint = findWaypointByName(player.getCurrentMapId(), waypointName);
        if (currentWaypoint == null) {
            return TeleportResult.error("找不到传送点: " + waypointName);
        }

        // 检查玩家是否在传送点附近（九宫格范围内）
        int dx = Math.abs(player.getX() - currentWaypoint.getX());
        int dy = Math.abs(player.getY() - currentWaypoint.getY());
        if (dx > 1 || dy > 1) {
            return TeleportResult.error("你不在传送点附近，请先移动到传送点");
        }

        // 查找目标传送点
        WaypointConfig targetWaypoint = findWaypointByDisplayName(targetDisplayName, currentWaypoint.getConnectedWaypointIds());
        if (targetWaypoint == null) {
            return TeleportResult.error("无法传送到: " + targetDisplayName + "，该目的地不在可传送列表中");
        }

        // 获取目标地图配置
        MapConfig targetMapConfig = configDataManager.getMap(targetWaypoint.getMapId());
        if (targetMapConfig == null) {
            return TeleportResult.error("目标地图不存在: " + targetWaypoint.getMapId());
        }

        // 执行传送：更新玩家位置和地图
        String oldMapId = player.getCurrentMapId();
        player.setCurrentMapId(targetWaypoint.getMapId());
        player.setX(targetWaypoint.getX());
        player.setY(targetWaypoint.getY());

        // 如果传送到安全区，恢复生命和法力
        boolean healthRestored = false;
        if (targetMapConfig.isSafe()) {
            player.setCurrentHealth(player.getMaxHealth());
            player.setCurrentMana(player.getMaxMana());
            healthRestored = true;
        }

        playerRepository.save(player);

        // 构建传送成功消息
        String message = String.format("传送成功！从 %s 传送到 %s·%s (位置: %d, %d)",
                oldMapId, targetMapConfig.getName(), targetWaypoint.getName(),
                targetWaypoint.getX(), targetWaypoint.getY());

        if (healthRestored) {
            message += "\n【安全区域】生命和法力已恢复";
        }

        return TeleportResult.success(message, targetWaypoint.getMapId(),
                targetWaypoint.getX(), targetWaypoint.getY(), healthRestored);
    }

    /**
     * 根据名称查找地图上的传送点配置
     */
    private WaypointConfig findWaypointByName(String mapId, String waypointName) {
        for (WaypointConfig wp : configDataManager.getAllWaypoints()) {
            if (wp.getMapId().equals(mapId) && wp.getName().equals(waypointName)) {
                return wp;
            }
        }
        return null;
    }

    /**
     * 根据显示名称（地图名·传送点名）查找传送点配置
     * @param displayName 显示名称（格式：地图名·传送点名）
     * @param connectedWaypointIds 允许的传送点ID列表
     */
    private WaypointConfig findWaypointByDisplayName(String displayName, List<String> connectedWaypointIds) {
        if (connectedWaypointIds == null || connectedWaypointIds.isEmpty()) {
            return null;
        }

        for (String wpId : connectedWaypointIds) {
            WaypointConfig wp = configDataManager.getWaypoint(wpId);
            if (wp != null) {
                MapConfig mapConfig = configDataManager.getMap(wp.getMapId());
                String mapName = mapConfig != null ? mapConfig.getName() : wp.getMapId();
                String wpDisplayName = mapName + "·" + wp.getName();
                if (wpDisplayName.equals(displayName)) {
                    return wp;
                }
            }
        }
        return null;
    }
}
