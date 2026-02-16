package com.heibai.clawworld.infrastructure.persistence.repository;

import com.heibai.clawworld.infrastructure.persistence.entity.PlayerEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * 玩家持久化仓储接口
 */
@Repository
public interface PlayerRepository extends MongoRepository<PlayerEntity, String> {
    // 玩家仓储只负责游戏数据的持久化
    // 账号相关的查询（用户名、昵称、会话ID）请使用AccountRepository
}
