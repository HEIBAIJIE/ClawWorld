package com.heibai.clawworld.infrastructure.config.data.map;

import lombok.Data;

/**
 * 地图地形配置 - 从CSV读取，用于覆盖默认地形
 * 使用矩形区域存储，(x1,y1) 是左下角，(x2,y2) 是右上角
 */
@Data
public class MapTerrainConfig {
    private String mapId;
    private int x1;  // 左下角 x
    private int y1;  // 左下角 y
    private int x2;  // 右上角 x
    private int y2;  // 右上角 y
    private String terrainTypes;
}
