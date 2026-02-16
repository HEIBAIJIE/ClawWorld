package com.heibai.clawworld.infrastructure.persistence.repository;

import com.heibai.clawworld.infrastructure.persistence.entity.EnemyInstanceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 敌人实例持久化仓储接口
 */
@Repository
public interface EnemyInstanceRepository extends MongoRepository<EnemyInstanceEntity, String> {

    /**
     * 根据地图ID和实例ID查找敌人
     */
    Optional<EnemyInstanceEntity> findByMapIdAndInstanceId(String mapId, String instanceId);

    /**
     * 查找地图上的所有敌人实例
     */
    List<EnemyInstanceEntity> findByMapId(String mapId);

    /**
     * 查找所有已死亡的敌人
     */
    List<EnemyInstanceEntity> findByDead(boolean dead);

    /**
     * 查找需要刷新的敌人（已死亡且死亡时间早于指定时间）
     */
    List<EnemyInstanceEntity> findByDeadAndLastDeathTimeBefore(boolean dead, Long time);
}
