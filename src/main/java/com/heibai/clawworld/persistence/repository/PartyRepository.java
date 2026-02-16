package com.heibai.clawworld.persistence.repository;

import com.heibai.clawworld.domain.character.Party;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 队伍持久化仓储接口
 * Party领域对象已经包含@Document注解，可以直接使用
 */
@Repository
public interface PartyRepository extends MongoRepository<Party, String> {

    /**
     * 根据队长ID查找队伍
     */
    Optional<Party> findByLeaderId(String leaderId);

    /**
     * 查找包含指定成员的队伍
     */
    Optional<Party> findByMemberIdsContaining(String memberId);

    /**
     * 查找所有成员数量大于指定值的队伍
     */
    @Query("{ 'memberIds': { $exists: true, $not: { $size: 0 } } }")
    List<Party> findAllNonEmptyParties();
}
