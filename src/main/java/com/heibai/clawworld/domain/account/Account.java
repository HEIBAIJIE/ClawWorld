package com.heibai.clawworld.domain.account;

import lombok.Data;

/**
 * 账号领域对象
 * 根据设计文档：玩家的认证信息（包括用户名、昵称、密码）保存在账号中（Account），一个账号对应一个玩家
 */
@Data
public class Account {
    private String id;

    // 认证信息
    private String username;  // 用户名，唯一
    private String nickname;  // 昵称，唯一
    private String password;  // 密码

    // 关联的玩家ID
    private String playerId;

    // 会话信息
    private String sessionId;
    private boolean online;
    private Long lastLoginTime;
    private Long lastLogoutTime;
}
