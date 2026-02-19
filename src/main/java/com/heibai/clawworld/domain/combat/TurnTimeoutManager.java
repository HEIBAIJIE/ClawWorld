package com.heibai.clawworld.domain.combat;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 回合超时管理器
 * 负责管理玩家回合的超时机制
 *
 * 工作原理：
 * 1. 当轮到某个玩家的回合时，启动12秒定时器（前端显示10秒，留2秒缓冲）
 * 2. 如果玩家在12秒内行动，取消定时器
 * 3. 如果12秒超时，自动执行空过（通过回调通知CombatEngine）
 */
@Slf4j
public class TurnTimeoutManager {

    // 回合超时时间：12秒（前端显示10秒，留2秒缓冲避免前端自动wait与后端超时冲突）
    private static final long TURN_TIMEOUT_SECONDS = 12;

    // 定时器线程池
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "TurnTimeout");
        t.setDaemon(true);
        return t;
    });

    // 当前活跃的超时任务（key: combatId, value: 超时任务）
    private final Map<String, ScheduledFuture<?>> activeTimeouts = new ConcurrentHashMap<>();

    // 当前回合的玩家（key: combatId, value: characterId）
    private final Map<String, String> currentTurnPlayers = new ConcurrentHashMap<>();

    // 回合开始时间（key: combatId, value: 开始时间戳）
    private final Map<String, Long> turnStartTimes = new ConcurrentHashMap<>();

    // 超时回调接口
    private final TurnTimeoutCallback callback;

    public TurnTimeoutManager(TurnTimeoutCallback callback) {
        this.callback = callback;
    }

    /**
     * 开始玩家回合计时
     *
     * @param combatId 战斗ID
     * @param characterId 玩家角色ID
     */
    public synchronized void startPlayerTurn(String combatId, String characterId) {
        // 取消之前的超时任务（如果有）
        cancelTimeoutInternal(combatId);

        // 记录当前回合玩家和开始时间
        currentTurnPlayers.put(combatId, characterId);
        turnStartTimes.put(combatId, System.currentTimeMillis());

        log.debug("[战斗 {}] 开始玩家 {} 的回合计时，{}秒后超时", combatId, characterId, TURN_TIMEOUT_SECONDS);

        // 启动超时任务
        ScheduledFuture<?> future = scheduler.schedule(() -> {
            handleTimeout(combatId, characterId);
        }, TURN_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        activeTimeouts.put(combatId, future);
    }

    /**
     * 玩家行动完成，取消超时计时
     *
     * @param combatId 战斗ID
     * @param characterId 玩家角色ID
     */
    public synchronized void playerActed(String combatId, String characterId) {
        String currentPlayer = currentTurnPlayers.get(combatId);
        if (currentPlayer != null && currentPlayer.equals(characterId)) {
            log.debug("[战斗 {}] 玩家 {} 已行动，取消超时计时", combatId, characterId);
            cancelTimeoutInternal(combatId);
            currentTurnPlayers.remove(combatId);
            turnStartTimes.remove(combatId);
        }
    }

    /**
     * 取消超时计时（内部方法，不加锁）
     */
    private void cancelTimeoutInternal(String combatId) {
        ScheduledFuture<?> future = activeTimeouts.remove(combatId);
        if (future != null && !future.isDone()) {
            future.cancel(false);
        }
    }

    /**
     * 取消超时计时
     */
    public synchronized void cancelTimeout(String combatId) {
        cancelTimeoutInternal(combatId);
    }

    /**
     * 战斗结束，清理所有相关资源
     */
    public synchronized void combatEnded(String combatId) {
        cancelTimeoutInternal(combatId);
        currentTurnPlayers.remove(combatId);
        turnStartTimes.remove(combatId);
    }

    /**
     * 处理超时
     */
    private void handleTimeout(String combatId, String characterId) {
        // 先检查是否仍然是该玩家的回合（可能已经被取消）
        synchronized (this) {
            String currentPlayer = currentTurnPlayers.get(combatId);
            if (currentPlayer == null || !currentPlayer.equals(characterId)) {
                // 已经不是该玩家的回合了，忽略超时
                log.debug("[战斗 {}] 玩家 {} 的超时已被取消，忽略", combatId, characterId);
                return;
            }

            // 清理状态
            activeTimeouts.remove(combatId);
            currentTurnPlayers.remove(combatId);
            turnStartTimes.remove(combatId);
        }

        log.info("[战斗 {}] 玩家 {} 回合超时，自动空过", combatId, characterId);

        // 通知回调（在锁外执行，避免死锁）
        if (callback != null) {
            try {
                callback.onTurnTimeout(combatId, characterId);
            } catch (Exception e) {
                log.error("[战斗 {}] 处理回合超时回调失败", combatId, e);
            }
        }
    }

    /**
     * 获取当前回合的玩家
     */
    public String getCurrentTurnPlayer(String combatId) {
        return currentTurnPlayers.get(combatId);
    }

    /**
     * 获取当前回合开始时间（毫秒时间戳）
     */
    public long getTurnStartTime(String combatId) {
        Long startTime = turnStartTimes.get(combatId);
        return startTime != null ? startTime : 0;
    }

    /**
     * 获取当前回合已经过的时间（毫秒）
     */
    public long getElapsedTime(String combatId) {
        Long startTime = turnStartTimes.get(combatId);
        if (startTime == null) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 检查当前回合是否已超时（用于惰性检查）
     */
    public boolean isCurrentTurnTimeout(String combatId) {
        Long startTime = turnStartTimes.get(combatId);
        if (startTime == null) {
            return false;
        }
        return System.currentTimeMillis() - startTime > TURN_TIMEOUT_SECONDS * 1000;
    }

    /**
     * 关闭管理器
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 回合超时回调接口
     */
    public interface TurnTimeoutCallback {
        /**
         * 当玩家回合超时时调用
         *
         * @param combatId 战斗ID
         * @param characterId 超时的玩家角色ID
         */
        void onTurnTimeout(String combatId, String characterId);
    }
}
