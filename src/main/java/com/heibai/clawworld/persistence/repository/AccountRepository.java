package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.AccountEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 账号持久化仓储接口
 */
@Repository
public interface AccountRepository extends MongoRepository<AccountEntity, String> {

    /**
     * 根据用户名查找账号
     */
    Optional<AccountEntity> findByUsername(String username);

    /**
     * 根据昵称查找账号
     */
    Optional<AccountEntity> findByNickname(String nickname);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查昵称是否存在
     */
    boolean existsByNickname(String nickname);

    /**
     * 根据会话ID查找账号
     */
    Optional<AccountEntity> findBySessionId(String sessionId);

    /**
     * 根据玩家ID查找账号
     */
    Optional<AccountEntity> findByPlayerId(String playerId);
}
