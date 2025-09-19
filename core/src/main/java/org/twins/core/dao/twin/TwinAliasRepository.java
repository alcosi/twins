package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.enums.twin.TwinAliasType;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAliasRepository extends CrudRepository<TwinAliasEntity, UUID>, JpaSpecificationExecutor<TwinAliasEntity> {

    List<TwinAliasEntity> findAllByTwinId(UUID twinId);
    List<TwinAliasEntity> findAllByTwinIdAndArchivedFalse(UUID twinId);
    List<TwinAliasEntity> findAllByTwinIdIn(Collection<UUID> twinIds);
    List<TwinAliasEntity> findAllByTwinIdInAndArchivedFalse(Collection<UUID> twinIds);

    @Query("SELECT t FROM TwinAliasEntity t WHERE t.alias = :alias AND " +
            "(t.domainId = :domainId OR t.domainId IS NULL) AND " +
            "(t.businessAccountId = :businessAccountId OR t.businessAccountId IS NULL) AND " +
            "(t.userId = :userId OR t.userId IS NULL)")
    TwinAliasEntity findByAlias(@Param("alias") String alias,
                                @Param("domainId") UUID domainId,
                                @Param("businessAccountId") UUID businessAccountId,
                                @Param("userId") UUID userId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update domain set alias_counter = alias_counter + 1 " +
            "where id = :domainId ; " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias_value, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, 'D', concat(upper(domain.key), '-D', domain.alias_counter), now(), domain.id " +
            "from domain where :domainId = domain.id; " +
            "commit;")
    void createDomainAlias(@Param("twinId") UUID twinId, @Param("domainId") UUID domainId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update twin_class set domain_alias_counter = domain_alias_counter + 1 " +
            "where id = :twinClassId ; " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias_value, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, 'C', concat(upper(twin_class.key), '-C', twin_class.domain_alias_counter), now(), twin_class.domain_id " +
            "from twin_class where twin_class.id = :twinClassId ; " +
            "commit;")
    void createDomainClassAlias(@Param("twinId") UUID twinId, @Param("twinClassId") UUID twinClassId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "insert into twin_business_account_alias_counter (id, business_account_id, twin_class_id, alias_counter) " +
            "select gen_random_uuid(), :businessAccountId , :twinClassId , 1 " +
            "from twin where twin.id = :twinId " +
            "on conflict ON CONSTRAINT twin_business_account_alias_counter_uniq do update set alias_counter = twin_business_account_alias_counter.alias_counter + 1; " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias_value, created_at, business_account_id) " +
            "select gen_random_uuid(), :twinId, 'B', concat(upper(:twinClassKey) , '-B', twin_business_account_alias_counter.alias_counter), now(), :businessAccountId " +
            "from twin_business_account_alias_counter " +
            "where twin_business_account_alias_counter.business_account_id = :businessAccountId " +
            "and twin_business_account_alias_counter.twin_class_id = :twinClassId " +
            ";commit;")
    void createBusinessAccountClassAlias(@Param("twinId") UUID twinId, @Param("businessAccountId") UUID businessAccountId, @Param("twinClassId") UUID twinClassId, @Param("twinClassKey") String twinClassKey);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update space set domain_alias_counter = domain_alias_counter + 1 " +
            "where twin_id = :twinId and exists (select 1 from space where twin_id = :twinId); " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias_value, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, 'S', concat(upper(space.key), '-S', space.domain_alias_counter), now(), twin_class.domain_id " +
            "from space " +
            "join twin_class_schema_map map on space.twin_class_schema_id = map.twin_class_schema_id " +
            "join twin_class_schema twin_class on map.twin_class_id = twin_class.id " +
            "where space.twin_id = :twinId " +
            ";commit;")
    void createSpaceDomainAlias(@Param("twinId") UUID twinId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update space set business_account_alias_counter = business_account_alias_counter + 1 " +
            "where twin_id = :twinId and :aliasType in ('K', 'T') and exists (select 1 from space where twin_id = :twinId); " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias_value, created_at, domain_id, business_account_id) " +
            "select gen_random_uuid(), :twinId, :aliasType, concat(upper(space.key), '-', :aliasType, space.business_account_alias_counter), now(), twin_class.domain_id, twin.owner_business_account_id " +
            "from space " +
            "join twin_class_schema_map map on space.twin_class_schema_id = map.twin_class_schema_id " +
            "join twin_class_schema twin_class on map.twin_class_id = twin_class.id " +
            "join twin on twin.id = space.twin_id " +
            "where space.twin_id = :twinId " +
            ";commit;")
    void createSpaceBusinessAccountAlias(@Param("twinId") UUID twinId, @Param("aliasType") String aliasType);

    @Query("SELECT t FROM TwinAliasEntity t WHERE t.twinId = :twinId AND t.aliasTypeId = :aliasType")
    TwinAliasEntity findByTwinIdAndType(@Param("twinId") UUID twinId, @Param("aliasType") TwinAliasType aliasType);

    @Query("SELECT t.id FROM TwinAliasEntity t WHERE t.businessAccountId = :businessAccountId AND t.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);
}
