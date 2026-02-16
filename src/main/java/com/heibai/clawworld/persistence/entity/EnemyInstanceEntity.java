package com.heibai.clawworld.persistence.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 敌人运行时实例持久化实体
 * 存储敌人的运行时状态，如当前生命值、死亡时间等
 * 敌人的模板配置从CSV读取并缓存，不需要持久化
 */
@Data
@Document(collection = "enemy_instances")
@CompoundIndex(name = "map_instance_idx", def = "{'mapId': 1, 'instanceId': 1}", unique = true)
public class EnemyInstanceEntity {
    @Id
    private String id;

    /**
     * 地图ID
     */
    private String mapId;

    /**
     * 实例ID（例如：goblin_1, goblin_2）
     */
    private String instanceId;

    /**
     * 敌人模板ID（例如：goblin）
     */
    private String templateId;

    /**
     * 当前生命值
     */
    private int currentHealth;

    /**
     * 当前法力值
     */
    private int currentMana;

    /**
     * 是否已死亡
     */
    private boolean dead;

    /**
     * 最后死亡时间（毫秒时间戳）
     */
    private Long lastDeathTime;

    /**
     * 是否在战斗中
     */
    private boolean inCombat;

    /**
     * 战斗ID
     */
    private String combatId;

    /**
     * 位置信息
     */
    private int x;
    private int y;
}
