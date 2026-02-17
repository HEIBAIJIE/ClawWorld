package com.heibai.clawworld.application.service;

import java.util.List;
import java.util.Set;

/**
 * 寻路服务
 * 负责 A* 寻路算法、BFS 可达性计算、位置可通行性检查
 */
public interface PathfindingService {

    /**
     * 使用 A* 算法寻找从起点到终点的最短路径
     * @param mapId 地图ID
     * @param startX 起点X坐标
     * @param startY 起点Y坐标
     * @param targetX 终点X坐标
     * @param targetY 终点Y坐标
     * @return 路径点列表（不包含起点，包含终点），如果无法到达返回null
     */
    List<int[]> findPath(String mapId, int startX, int startY, int targetX, int targetY);

    /**
     * 使用 BFS 计算从指定位置出发的可达性地图
     * @param mapId 地图ID
     * @param startX 起点X坐标
     * @param startY 起点Y坐标
     * @return 可达位置集合，格式为 "x,y"
     */
    Set<String> calculateReachabilityMap(String mapId, int startX, int startY);

    /**
     * 检查指定位置是否可通行
     * 根据设计文档：树、岩石、山脉、河流、海洋、墙不可通过
     * 在消灭敌人以前，无法进入敌人所在的格子
     * @param mapId 地图ID
     * @param x X坐标
     * @param y Y坐标
     * @return 是否可通行
     */
    boolean isPositionPassable(String mapId, int x, int y);
}
