package com.heibai.clawworld.infrastructure.persistence.repository;

import com.heibai.clawworld.infrastructure.persistence.entity.EnemyInstanceEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 敌人实例Repository单元测试
 */
@DataMongoTest
class EnemyInstanceRepositoryTest {

    @Autowired
    private EnemyInstanceRepository enemyInstanceRepository;

    private EnemyInstanceEntity testEnemy;

    @BeforeEach
    void setUp() {
        testEnemy = new EnemyInstanceEntity();
        testEnemy.setMapId("test_map");
        testEnemy.setInstanceId("slime_1");
        testEnemy.setTemplateId("slime");
        testEnemy.setCurrentHealth(50);
        testEnemy.setCurrentMana(0);
        testEnemy.setDead(false);
        testEnemy.setInCombat(false);
        testEnemy.setX(10);
        testEnemy.setY(10);
    }

    @AfterEach
    void tearDown() {
        enemyInstanceRepository.deleteAll();
    }

    @Test
    void testSaveAndFindById() {
        EnemyInstanceEntity saved = enemyInstanceRepository.save(testEnemy);
        assertNotNull(saved.getId());

        Optional<EnemyInstanceEntity> found = enemyInstanceRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("slime_1", found.get().getInstanceId());
    }

    @Test
    void testFindByMapIdAndInstanceId() {
        enemyInstanceRepository.save(testEnemy);

        Optional<EnemyInstanceEntity> found =
            enemyInstanceRepository.findByMapIdAndInstanceId("test_map", "slime_1");
        assertTrue(found.isPresent());
        assertEquals("slime", found.get().getTemplateId());
    }

    @Test
    void testFindByMapId() {
        enemyInstanceRepository.save(testEnemy);

        EnemyInstanceEntity enemy2 = new EnemyInstanceEntity();
        enemy2.setMapId("test_map");
        enemy2.setInstanceId("slime_2");
        enemy2.setTemplateId("slime");
        enemy2.setCurrentHealth(50);
        enemy2.setCurrentMana(0);
        enemy2.setDead(false);
        enemy2.setX(15);
        enemy2.setY(15);
        enemyInstanceRepository.save(enemy2);

        List<EnemyInstanceEntity> enemies = enemyInstanceRepository.findByMapId("test_map");
        assertEquals(2, enemies.size());
    }

    @Test
    void testFindByIsDead() {
        testEnemy.setDead(true);
        testEnemy.setLastDeathTime(System.currentTimeMillis());
        enemyInstanceRepository.save(testEnemy);

        List<EnemyInstanceEntity> deadEnemies = enemyInstanceRepository.findByDead(true);
        assertEquals(1, deadEnemies.size());
        assertTrue(deadEnemies.get(0).isDead());
    }

    @Test
    void testFindByIsDeadAndLastDeathTimeBefore() {
        long now = System.currentTimeMillis();
        testEnemy.setDead(true);
        testEnemy.setLastDeathTime(now - 120000); // 2分钟前死亡
        enemyInstanceRepository.save(testEnemy);

        // 查找1分钟前死亡的敌人
        List<EnemyInstanceEntity> enemies =
            enemyInstanceRepository.findByDeadAndLastDeathTimeBefore(true, now - 60000);
        assertEquals(1, enemies.size());
    }

    @Test
    void testUpdateEnemyState() {
        EnemyInstanceEntity saved = enemyInstanceRepository.save(testEnemy);

        // 更新敌人状态
        saved.setCurrentHealth(0);
        saved.setDead(true);
        saved.setLastDeathTime(System.currentTimeMillis());
        enemyInstanceRepository.save(saved);

        Optional<EnemyInstanceEntity> updated = enemyInstanceRepository.findById(saved.getId());
        assertTrue(updated.isPresent());
        assertTrue(updated.get().isDead());
        assertEquals(0, updated.get().getCurrentHealth());
        assertNotNull(updated.get().getLastDeathTime());
    }
}
