package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.CombatEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 战斗状态持久化仓储接口
 */
@Repository
public interface CombatRepository extends MongoRepository<CombatEntity, String> {

    /**
     * 查找指定地图上的所有进行中的战斗
     */
    List<CombatEntity> findByMapIdAndStatus(String mapId, CombatEntity.CombatStatus status);

    /**
     * 查找所有进行中的战斗
     */
    List<CombatEntity> findByStatus(CombatEntity.CombatStatus status);

    /**
     * 查找超时的战斗（开始时间早于指定时间且状态为进行中）
     */
    List<CombatEntity> findByStatusAndStartTimeBefore(CombatEntity.CombatStatus status, Long time);
}
