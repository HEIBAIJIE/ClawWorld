package com.heibai.clawworld.domain.map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 传送点领域对象
 * 根据设计文档：传送点是一种特殊的地图实体，可交互
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Waypoint extends MapEntity {
    /**
     * 可以传送到的其他传送点ID列表
     */
    private List<String> connectedWaypointIds;

    /**
     * 连接的传送点显示信息列表（格式：地图名·传送点名）
     * 由外部服务设置，用于生成交互选项
     */
    private List<String> connectedWaypointDisplayNames;

    @Override
    public boolean isPassable() {
        return true; // 传送点可通过
    }

    @Override
    public boolean isInteractable() {
        return true; // 传送点可交互
    }

    @Override
    public String getEntityType() {
        return "WAYPOINT";
    }

    @Override
    public List<String> getInteractionOptions() {
        // 如果有连接的传送点显示名称，生成具体的传送选项
        if (connectedWaypointDisplayNames != null && !connectedWaypointDisplayNames.isEmpty()) {
            List<String> options = new ArrayList<>();
            for (String displayName : connectedWaypointDisplayNames) {
                options.add("传送到" + displayName);
            }
            return options;
        }
        // 如果没有设置显示名称，返回空列表（不应该发生）
        return new ArrayList<>();
    }
}
