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

    @Query("SELECT t FROM TwinAliasEntity t WHERE t.alias = :alias AND " +
            "(t.domainId = :domainId OR :domainId IS NULL) AND " +
            "(t.businessAccountId = :businessAccountId OR :businessAccountId IS NULL) AND " +
            "(t.userId = :userId OR :userId IS NULL)")
    TwinAliasEntity findByAlias(@Param("alias") String alias,
                                @Param("domainId") UUID domainId,
                                @Param("businessAccountId") UUID businessAccountId,
                                @Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update twin_class set domain_alias_counter = twin_class.domain_alias_counter + 1 " +
            "where twin_class.id = (select twin_class_id from twin where id = :twinId) and " +
            "twin_class.domain_alias_counter IS NOT NULL; " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, :aliasType, concat(twin_class.key, '-', twin_class.domain_alias_counter), now(), twin_class.domain_id " +
            "from twin_class, twin where twin.id = :twinId and twin.twin_class_id = twin_class.id; " +
            "COMMIT;")
    void createAliasByClass(@Param("twinId") UUID twinId, @Param("aliasType") String aliasType);

    @Query("SELECT t FROM TwinAliasEntity t WHERE t.twinId = :twinId AND t.aliasTypeId = :aliasType")
    TwinAliasEntity findByTwinIdAndType(@Param("twinId") UUID twinId, @Param("aliasType") String aliasType);

    @Query("SELECT t.id FROM TwinAliasEntity t WHERE t.businessAccountId = :businessAccountId AND t.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);
}
