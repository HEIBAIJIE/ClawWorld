package com.heibai.clawworld.application.service;

/**
 * 传送服务
 * 负责处理传送点交互
 */
public interface TeleportService {

    /**
     * 执行传送
     * @param playerId 玩家ID
     * @param waypointName 当前传送点名称
     * @param targetDisplayName 目标传送点显示名称（格式：地图名·传送点名）
     * @return 传送结果
     */
    TeleportResult teleport(String playerId, String waypointName, String targetDisplayName);

    /**
     * 传送结果
     */
    class TeleportResult {
        private boolean success;
        private String message;
        private String newMapId;
        private int newX;
        private int newY;
        private boolean healthRestored;
        private boolean windowChanged;

        public static TeleportResult success(String message, String newMapId, int newX, int newY, boolean healthRestored) {
            TeleportResult result = new TeleportResult();
            result.success = true;
            result.message = message;
            result.newMapId = newMapId;
            result.newX = newX;
            result.newY = newY;
            result.healthRestored = healthRestored;
            result.windowChanged = true;
            return result;
        }

        public static TeleportResult error(String message) {
            TeleportResult result = new TeleportResult();
            result.success = false;
            result.message = message;
            return result;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getNewMapId() {
            return newMapId;
        }

        public int getNewX() {
            return newX;
        }

        public int getNewY() {
            return newY;
        }

        public boolean isHealthRestored() {
            return healthRestored;
        }

        public boolean isWindowChanged() {
            return windowChanged;
        }
    }
}
