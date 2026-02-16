package com.heibai.clawworld.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private boolean success;
    private String message;
    private String sessionId;
    private String backgroundPrompt;
    private String windowContent;

    public static LoginResponse success(String sessionId, String backgroundPrompt, String windowContent) {
        return LoginResponse.builder()
                .success(true)
                .message("登录成功")
                .sessionId(sessionId)
                .backgroundPrompt(backgroundPrompt)
                .windowContent(windowContent)
                .build();
    }

    public static LoginResponse error(String message) {
        return LoginResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
