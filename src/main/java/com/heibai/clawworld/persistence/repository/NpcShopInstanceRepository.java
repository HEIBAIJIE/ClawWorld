package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.persistence.entity.NpcShopInstanceEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * NPC商店实例持久化仓储接口
 */
@Repository
public interface NpcShopInstanceRepository extends MongoRepository<NpcShopInstanceEntity, String> {

    /**
     * 根据NPC ID查找商店实例
     */
    Optional<NpcShopInstanceEntity> findByNpcId(String npcId);

    /**
     * 查找地图上的所有NPC商店
     */
    List<NpcShopInstanceEntity> findByMapId(String mapId);

    /**
     * 查找需要刷新的商店（上次刷新时间早于指定时间）
     */
    List<NpcShopInstanceEntity> findByLastRefreshTimeBefore(Long time);
}
