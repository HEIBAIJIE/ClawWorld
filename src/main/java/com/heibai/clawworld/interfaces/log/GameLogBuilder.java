package com.heibai.clawworld.interfaces.log;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 游戏日志构建器
 * 用于构建完整的游戏响应日志
 */
public class GameLogBuilder {

    private final List<GameLog> logs = new ArrayList<>();

    /**
     * 添加日志
     */
    public GameLogBuilder addLog(GameLog log) {
        logs.add(log);
        return this;
    }

    /**
     * 添加服务端背景日志
     */
    public GameLogBuilder addBackground(String subType, String content) {
        logs.add(GameLog.serverBackground(subType, content));
        return this;
    }

    /**
     * 添加服务端窗口日志
     */
    public GameLogBuilder addWindow(String subType, String content) {
        logs.add(GameLog.serverWindow(subType, content));
        return this;
    }

    /**
     * 添加服务端状态日志
     */
    public GameLogBuilder addState(String subType, String content) {
        logs.add(GameLog.serverState(subType, content));
        return this;
    }

    /**
     * 构建最终的日志字符串
     */
    public String build() {
        return logs.stream()
            .map(GameLog::format)
            .collect(Collectors.joining("\n"));
    }

    /**
     * 获取所有日志
     */
    public List<GameLog> getLogs() {
        return new ArrayList<>(logs);
    }

    /**
     * 清空日志
     */
    public void clear() {
        logs.clear();
    }
}
