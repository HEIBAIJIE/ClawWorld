package com.heibai.clawworld.domain.map;

import lombok.Data;

import java.util.List;

/**
 * 地图领域对象 - 运行时使用
 * 地图的基础配置（宽度、高度、地形等）从CSV读取并缓存
 * 这个对象主要用于运行时的地图状态管理
 */
@Data
public class GameMap {
    private String id;
    private String name;
    private String description;
    private int width;
    private int height;
    private boolean isSafe;
    private Integer recommendedLevel;

    // 地形数据：二维数组，每个格子存储地形类型列表
    private List<List<TerrainCell>> terrain;

    // 地图上的实体列表（运行时动态变化）
    private List<MapEntity> entities;

    @Data
    public static class TerrainCell {
        private List<String> terrainTypes;
        private boolean passable; // 是否可通过（综合地形和实体计算）
    }
}
