package com.heibai.clawworld.application.impl.window;

import com.heibai.clawworld.domain.character.Player;
import com.heibai.clawworld.domain.chat.ChatMessage;
import com.heibai.clawworld.domain.map.GameMap;

import java.util.List;

/**
 * 窗口内容生成器接口
 * 每种窗口类型都有自己的实现
 */
public interface WindowContentGenerator {

    /**
     * 生成窗口内容
     * @param context 生成上下文
     * @return 窗口内容文本
     */
    String generateContent(WindowContext context);

    /**
     * 窗口生成上下文
     */
    class WindowContext {
        private String playerId;
        private String windowId;
        private Player player;
        private GameMap map;
        private List<ChatMessage> chatHistory;
        private Object additionalData;

        public static WindowContextBuilder builder() {
            return new WindowContextBuilder();
        }

        public String getPlayerId() { return playerId; }
        public String getWindowId() { return windowId; }
        public Player getPlayer() { return player; }
        public GameMap getMap() { return map; }
        public List<ChatMessage> getChatHistory() { return chatHistory; }
        public Object getAdditionalData() { return additionalData; }

        public static class WindowContextBuilder {
            private final WindowContext context = new WindowContext();

            public WindowContextBuilder playerId(String playerId) {
                context.playerId = playerId;
                return this;
            }

            public WindowContextBuilder windowId(String windowId) {
                context.windowId = windowId;
                return this;
            }

            public WindowContextBuilder player(Player player) {
                context.player = player;
                return this;
            }

            public WindowContextBuilder map(GameMap map) {
                context.map = map;
                return this;
            }

            public WindowContextBuilder chatHistory(List<ChatMessage> chatHistory) {
                context.chatHistory = chatHistory;
                return this;
            }

            public WindowContextBuilder additionalData(Object data) {
                context.additionalData = data;
                return this;
            }

            public WindowContext build() {
                return context;
            }
        }
    }
}
