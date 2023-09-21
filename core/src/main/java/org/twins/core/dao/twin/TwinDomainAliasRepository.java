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
public interface TwinDomainAliasRepository extends CrudRepository<TwinDomainAliasEntity, UUID>, JpaSpecificationExecutor<TwinDomainAliasEntity> {
    List<TwinDomainAliasEntity> findAllByTwinId(UUID twinId);

    TwinDomainAliasEntity findByDomainIdAndAlias(UUID domainId, String alias);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update twin_class set domain_alias_counter = twin_class.domain_alias_counter + 1 from twin where twin.twin_class_id = twin_class.id and twin.id = :twinId ; " +
            "insert into twin_domain_alias(id, twin_id, domain_id, alias) select gen_random_uuid (), :twinId , twin_class.domain_id, twin_class.key || '-' || twin_class.domain_alias_counter " +
            "from twin_class, twin " +
            "where twin_class.id = twin.twin_class_id and twin.id = :twinId ; " +
            "COMMIT;")
    void createAliasByClass(@Param("twinId") UUID twinId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "begin; " +
            "update space set domain_alias_counter = domain_alias_counter + 1 where twin_id = :spaceTwinId ;" +
            "insert into twin_domain_alias(id, twin_id, domain_id, alias) select gen_random_uuid (), :twinId , twin_class.domain_id, space.key || '-' || space.domain_alias_counter " +
            "from space, twin, twin_class " +
            "where space.twin_id = :spaceTwinId and twin.id = :twinId and twin.twin_class_id = twin_class.id; " +
            "COMMIT;")
    void createAliasBySpace(UUID twinId, UUID spaceTwinId);
}
