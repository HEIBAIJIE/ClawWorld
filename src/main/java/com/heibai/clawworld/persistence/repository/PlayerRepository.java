package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.PlayerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 玩家持久化仓储接口
 */
@Repository
public interface PlayerRepository extends MongoRepository<PlayerEntity, String> {

    /**
     * 根据用户名查找玩家
     */
    Optional<PlayerEntity> findByUsername(String username);

    /**
     * 根据昵称查找玩家
     */
    Optional<PlayerEntity> findByNickname(String nickname);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查昵称是否存在
     */
    boolean existsByNickname(String nickname);

    /**
     * 根据会话ID查找玩家
     */
    Optional<PlayerEntity> findBySessionId(String sessionId);
}
