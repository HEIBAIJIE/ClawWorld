package com.heibai.clawworld.application.service;

import java.util.List;

/**
 * 对话服务
 * 负责处理 NPC 对话交互
 */
public interface DialogueService {

    /**
     * 与 NPC 对话
     * @param playerId 玩家ID
     * @param npcName NPC名称
     * @return 对话结果
     */
    DialogueResult talk(String playerId, String npcName);

    /**
     * 对话结果
     */
    class DialogueResult {
        private boolean success;
        private String message;
        private List<String> dialogueLines;

        public static DialogueResult success(String message, List<String> dialogueLines) {
            DialogueResult result = new DialogueResult();
            result.success = true;
            result.message = message;
            result.dialogueLines = dialogueLines;
            return result;
        }

        public static DialogueResult error(String message) {
            DialogueResult result = new DialogueResult();
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

        public List<String> getDialogueLines() {
            return dialogueLines;
        }
    }
}
