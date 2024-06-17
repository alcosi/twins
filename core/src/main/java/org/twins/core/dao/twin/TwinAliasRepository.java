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
            "update domain set alias_counter = alias_counter + 1 " +
            "where id = (select domain_id from twin where id = :twinId); " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, :aliasType, concat(domain.key, '-', :aliasType, domain.alias_counter), now(), domain.id " +
            "from domain, twin where twin.id = :twinId and twin.domain_id = domain.id; " +
            "commit;")
    void createDomainAlias(@Param("twinId") UUID twinId, @Param("aliasType") TwinAliasType aliasType);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update twin_class set domain_alias_counter = domain_alias_counter + 1 " +
            "where id = (select twin_class_id from twin where id = :twinId); " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, :aliasType, concat(twin_class.key, '-', :aliasType, twin_class.domain_alias_counter), now(), twin_class.domain_id " +
            "from twin_class, twin where twin.id = :twinId and twin.twin_class_id = twin_class.id; " +
            "commit;")
    void createDomainClassAlias(@Param("twinId") UUID twinId, @Param("aliasType") TwinAliasType aliasType);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "insert into twin_business_account_alias_counter (id, business_account_id, twin_class_id, alias_counter) " +
            "select gen_random_uuid(), twin.owner_business_account_id, twin.twin_class_id, 1 " +
            "from twin where twin.id = :twinId " +
            "on conflict ON CONSTRAINT twin_business_account_alias_counter_uniq do update set alias_counter = twin_business_account_alias_counter.alias_counter + 1; " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias, created_at, business_account_id) " +
            "select gen_random_uuid(), :twinId, :aliasType, concat(twin_class.key, '-', :aliasType, twin_business_account_alias_counter.alias_counter), now(), twin.owner_business_account_id " +
            "from twin_business_account_alias_counter, twin_class, twin " +
            "where twin_business_account_alias_counter.business_account_id = twin.owner_business_account_id " +
            "and twin_business_account_alias_counter.twin_class_id = twin.twin_class_id " +
            "and twin_class.id = twin_business_account_alias_counter.twin_class_id " +
            "and twin.id = :twinId; " +
            "commit;")
    void createBusinessAccountClassAlias(@Param("twinId") UUID twinId, @Param("aliasType") TwinAliasType aliasType);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update space set domain_alias_counter = domain_alias_counter + 1 " +
            "where twin_id = :twinId; " +
            "insert into twin_alias(id, twin_id, twin_alias_type_id, alias, created_at, domain_id) " +
            "select gen_random_uuid(), :twinId, :aliasType, concat(space.key, '-', :aliasType, space.domain_alias_counter), now(), space.domain_id " +
            "from space where space.twin_id = :twinId; " +
            "commit;")
    void createSpaceAlias(@Param("twinId") UUID twinId, @Param("aliasType") TwinAliasType aliasType);

    @Query("SELECT t FROM TwinAliasEntity t WHERE t.twinId = :twinId AND t.aliasTypeId = :aliasType")
    TwinAliasEntity findByTwinIdAndType(@Param("twinId") UUID twinId, @Param("aliasType") TwinAliasType aliasType);

    @Query("SELECT t.id FROM TwinAliasEntity t WHERE t.businessAccountId = :businessAccountId AND t.domainId = :domainId")
    List<UUID> findAllByBusinessAccountIdAndDomainId(@Param("businessAccountId") UUID businessAccountId, @Param("domainId") UUID domainId);
}
