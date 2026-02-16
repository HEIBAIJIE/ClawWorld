package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.AccountEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 账号Repository单元测试
 * 使用内嵌MongoDB进行测试
 */
@DataMongoTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    private AccountEntity testAccount;

    @BeforeEach
    void setUp() {
        // 创建测试账号数据
        testAccount = new AccountEntity();
        testAccount.setUsername("testuser");
        testAccount.setNickname("测试玩家");
        testAccount.setPassword("password123");
        testAccount.setPlayerId("player_001");
        testAccount.setOnline(false);
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        accountRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        // 保存账号
        AccountEntity saved = accountRepository.save(testAccount);
        assertNotNull(saved.getId());

        // 根据ID查找
        Optional<AccountEntity> found = accountRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertEquals("测试玩家", found.get().getNickname());
    }

    @Test
    void testFindByUsername() {
        accountRepository.save(testAccount);

        Optional<AccountEntity> found = accountRepository.findByUsername("testuser");
        assertTrue(found.isPresent());
        assertEquals("测试玩家", found.get().getNickname());
    }

    @Test
    void testFindByNickname() {
        accountRepository.save(testAccount);

        Optional<AccountEntity> found = accountRepository.findByNickname("测试玩家");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testExistsByUsername() {
        assertFalse(accountRepository.existsByUsername("testuser"));

        accountRepository.save(testAccount);

        assertTrue(accountRepository.existsByUsername("testuser"));
    }

    @Test
    void testExistsByNickname() {
        assertFalse(accountRepository.existsByNickname("测试玩家"));

        accountRepository.save(testAccount);

        assertTrue(accountRepository.existsByNickname("测试玩家"));
    }

    @Test
    void testFindByPlayerId() {
        accountRepository.save(testAccount);

        Optional<AccountEntity> found = accountRepository.findByPlayerId("player_001");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testFindBySessionId() {
        testAccount.setSessionId("session_123");
        testAccount.setOnline(true);
        accountRepository.save(testAccount);

        Optional<AccountEntity> found = accountRepository.findBySessionId("session_123");
        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
        assertTrue(found.get().isOnline());
    }

    @Test
    void testUpdateAccount() {
        AccountEntity saved = accountRepository.save(testAccount);

        // 更新账号数据
        saved.setOnline(true);
        saved.setSessionId("new_session");
        saved.setLastLoginTime(System.currentTimeMillis());
        accountRepository.save(saved);

        // 验证更新
        Optional<AccountEntity> updated = accountRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isOnline());
        assertEquals("new_session", updated.get().getSessionId());
        assertNotNull(updated.get().getLastLoginTime());
    }

    @Test
    void testDeleteAccount() {
        AccountEntity saved = accountRepository.save(testAccount);
        assertNotNull(saved.getId());

        accountRepository.deleteById(saved.getId());

        Optional<AccountEntity> deleted = accountRepository.findById(saved.getId());
        assertFalse(deleted.isPresent());
    }

    @Test
    void testUniqueUsernameConstraint() {
        accountRepository.save(testAccount);

        // 尝试保存相同用户名的账号
        AccountEntity duplicate = new AccountEntity();
        duplicate.setUsername("testuser");
        duplicate.setNickname("另一个玩家");
        duplicate.setPassword("password456");

        // MongoDB的唯一索引会在保存时抛出异常
        assertThrows(org.springframework.dao.DuplicateKeyException.class, () -> {
            accountRepository.save(duplicate);
        });
    }

    @Test
    void testUniqueNicknameConstraint() {
        accountRepository.save(testAccount);

        // 尝试保存相同昵称的账号
        AccountEntity duplicate = new AccountEntity();
        duplicate.setUsername("anotheruser");
        duplicate.setNickname("测试玩家");
        duplicate.setPassword("password456");

        // MongoDB的唯一索引会在保存时抛出异常
        assertThrows(org.springframework.dao.DuplicateKeyException.class, () -> {
            accountRepository.save(duplicate);
        });
    }
}
