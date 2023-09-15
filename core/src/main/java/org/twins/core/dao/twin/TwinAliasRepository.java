package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAliasRepository extends CrudRepository<TwinAliasEntity, UUID>, JpaSpecificationExecutor<TwinAliasEntity> {
    List<TwinAliasEntity> findAllByTwinId(UUID twinId);

    TwinAliasEntity findByBusinessAccountIdAndAlias(UUID businessAccountId, String alias);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "insert into twin_alias_counter (id, business_account_id, twin_class_id, alias_counter) select gen_random_uuid(), twin.business_account_id, twin.twin_class_id, 1 " +
            "from twin where twin.id = :twinId " +
            "on conflict ON CONSTRAINT unique_business_account_id_twin_class_id do update set alias_counter = twin_alias_counter.alias_counter + 1; " +
            "insert into twin_alias(id, twin_id, business_account_id, alias) select gen_random_uuid (), :twinId , twin.business_account_id, twin_class.key || '-' || alias_counter " +
            "from twin_alias_counter, twin_class, twin " +
            "where twin_alias_counter.business_account_id = twin.business_account_id and twin_alias_counter.twin_class_id = twin.twin_class_id and twin_class.id = twin_alias_counter.twin_class_id and twin.id = :twinId ; " +
            "COMMIT;")
    void createAliasByClass(@Param("twinId") UUID twinId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update space set alias_counter = alias_counter + 1 where twin_id = :spaceTwinId ;" +
            "insert into twin_alias(id, twin_id, business_account_id, alias) select gen_random_uuid (), :twinId , twin.business_account_id, space.key || '-' || space.alias_counter " +
            "from space, twin " +
            "where space.twin_id = :spaceTwinId and twin.id = :twinId ; " +
            "COMMIT;")
    void createAliasBySpace(UUID twinId, UUID spaceTwinId);
}
