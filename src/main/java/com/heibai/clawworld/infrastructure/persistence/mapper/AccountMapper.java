package com.heibai.clawworld.infrastructure.persistence.mapper;

import com.heibai.clawworld.domain.account.Account;
import com.heibai.clawworld.infrastructure.persistence.entity.AccountEntity;
import org.springframework.stereotype.Component;

/**
 * 账号领域对象与持久化实体之间的映射器
 */
@Component
public class AccountMapper {

    /**
     * 将领域对象转换为持久化实体
     */
    public AccountEntity toEntity(Account account) {
        if (account == null) {
            return null;
        }

        AccountEntity entity = new AccountEntity();
        entity.setId(account.getId());
        entity.setUsername(account.getUsername());
        entity.setNickname(account.getNickname());
        entity.setPassword(account.getPassword());
        entity.setPlayerId(account.getPlayerId());
        entity.setSessionId(account.getSessionId());
        entity.setOnline(account.isOnline());
        entity.setLastLoginTime(account.getLastLoginTime());
        entity.setLastLogoutTime(account.getLastLogoutTime());

        return entity;
    }

    /**
     * 将持久化实体转换为领域对象
     */
    public Account toDomain(AccountEntity entity) {
        if (entity == null) {
            return null;
        }

        Account account = new Account();
        account.setId(entity.getId());
        account.setUsername(entity.getUsername());
        account.setNickname(entity.getNickname());
        account.setPassword(entity.getPassword());
        account.setPlayerId(entity.getPlayerId());
        account.setSessionId(entity.getSessionId());
        account.setOnline(entity.isOnline());
        account.setLastLoginTime(entity.getLastLoginTime());
        account.setLastLogoutTime(entity.getLastLogoutTime());

        return account;
    }
}
