package com.heibai.clawworld.application.service;

/**
 * 休息服务
 * 负责处理篝火休息等恢复交互
 */
public interface RestService {

    /**
     * 在篝火旁休息
     * @param playerId 玩家ID
     * @param targetName 目标名称（篝火等）
     * @return 休息结果
     */
    RestResult rest(String playerId, String targetName);

    /**
     * 休息结果
     */
    class RestResult {
        private boolean success;
        private String message;
        private int healthRestored;
        private int manaRestored;

        public static RestResult success(String message, int healthRestored, int manaRestored) {
            RestResult result = new RestResult();
            result.success = true;
            result.message = message;
            result.healthRestored = healthRestored;
            result.manaRestored = manaRestored;
            return result;
        }

        public static RestResult error(String message) {
            RestResult result = new RestResult();
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

        public int getHealthRestored() {
            return healthRestored;
        }

        public int getManaRestored() {
            return manaRestored;
        }
    }
}
