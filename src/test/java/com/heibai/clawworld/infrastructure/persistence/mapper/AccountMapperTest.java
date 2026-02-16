package com.heibai.clawworld.infrastructure.persistence.mapper;

import com.heibai.clawworld.domain.account.Account;
import com.heibai.clawworld.infrastructure.persistence.entity.AccountEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AccountMapper单元测试
 */
class AccountMapperTest {

    private AccountMapper accountMapper;
    private Account testAccount;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();

        // 创建测试账号
        testAccount = new Account();
        testAccount.setId("account_id");
        testAccount.setUsername("testuser");
        testAccount.setNickname("测试玩家");
        testAccount.setPassword("password123");
        testAccount.setPlayerId("player_001");
        testAccount.setSessionId("session_123");
        testAccount.setOnline(true);
        testAccount.setLastLoginTime(1234567890L);
        testAccount.setLastLogoutTime(1234567800L);
    }

    @Test
    void testToEntity() {
        AccountEntity entity = accountMapper.toEntity(testAccount);

        assertNotNull(entity);
        assertEquals("account_id", entity.getId());
        assertEquals("testuser", entity.getUsername());
        assertEquals("测试玩家", entity.getNickname());
        assertEquals("password123", entity.getPassword());
        assertEquals("player_001", entity.getPlayerId());
        assertEquals("session_123", entity.getSessionId());
        assertTrue(entity.isOnline());
        assertEquals(1234567890L, entity.getLastLoginTime());
        assertEquals(1234567800L, entity.getLastLogoutTime());
    }

    @Test
    void testToDomain() {
        AccountEntity entity = new AccountEntity();
        entity.setId("account_id");
        entity.setUsername("testuser");
        entity.setNickname("测试玩家");
        entity.setPassword("password123");
        entity.setPlayerId("player_001");
        entity.setSessionId("session_123");
        entity.setOnline(true);
        entity.setLastLoginTime(1234567890L);
        entity.setLastLogoutTime(1234567800L);

        Account account = accountMapper.toDomain(entity);

        assertNotNull(account);
        assertEquals("account_id", account.getId());
        assertEquals("testuser", account.getUsername());
        assertEquals("测试玩家", account.getNickname());
        assertEquals("password123", account.getPassword());
        assertEquals("player_001", account.getPlayerId());
        assertEquals("session_123", account.getSessionId());
        assertTrue(account.isOnline());
        assertEquals(1234567890L, account.getLastLoginTime());
        assertEquals(1234567800L, account.getLastLogoutTime());
    }

    @Test
    void testToEntityWithNullAccount() {
        AccountEntity entity = accountMapper.toEntity(null);
        assertNull(entity);
    }

    @Test
    void testToDomainWithNullEntity() {
        Account account = accountMapper.toDomain(null);
        assertNull(account);
    }

    @Test
    void testRoundTripConversion() {
        // 领域对象 -> 实体 -> 领域对象
        AccountEntity entity = accountMapper.toEntity(testAccount);
        Account converted = accountMapper.toDomain(entity);

        assertNotNull(converted);
        assertEquals(testAccount.getId(), converted.getId());
        assertEquals(testAccount.getUsername(), converted.getUsername());
        assertEquals(testAccount.getNickname(), converted.getNickname());
        assertEquals(testAccount.getPassword(), converted.getPassword());
        assertEquals(testAccount.getPlayerId(), converted.getPlayerId());
        assertEquals(testAccount.getSessionId(), converted.getSessionId());
        assertEquals(testAccount.isOnline(), converted.isOnline());
        assertEquals(testAccount.getLastLoginTime(), converted.getLastLoginTime());
        assertEquals(testAccount.getLastLogoutTime(), converted.getLastLogoutTime());
    }
}
