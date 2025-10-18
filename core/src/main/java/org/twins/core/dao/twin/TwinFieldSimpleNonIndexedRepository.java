package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface TwinFieldSimpleNonIndexedRepository extends CrudRepository<TwinFieldSimpleNonIndexedEntity , UUID>, JpaSpecificationExecutor<TwinFieldSimpleNonIndexedEntity> {

    List<TwinFieldSimpleNonIndexedEntity> findByTwinId(UUID twinId);
    List<TwinFieldSimpleNonIndexedEntity> findByTwinIdIn(Collection<UUID> twinIdList);
    boolean existsByTwinClassFieldId(UUID twinClassFieldId);
    boolean existsByTwinClassFieldIdAndValue(UUID twinClassFieldId, String value);

    @Query(value = """
            select distinct field.twinClassFieldId
            from TwinFieldSimpleNonIndexedEntity field where field.twin.twinClassId = :twinClassId and field.twinClassFieldId in (:twinClassFields)
            """)
    List<UUID> findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    void deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    @Transactional
    @Modifying
    @Query(value = "update TwinFieldSimpleNonIndexedEntity set twinClassFieldId = :toTwinClassFieldId where twinClassFieldId = :fromTwinClassFieldId and twin.twinClassId = :twinClassId")
    void replaceTwinClassFieldForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("fromTwinClassFieldId") UUID fromTwinClassFieldId, @Param("toTwinClassFieldId") UUID toTwinClassFieldId);

    void deleteByTwinIdAndTwinClassFieldIdIn(UUID twinId, Set<UUID> twinClassFieldIds);

    @Query(value = """
        select COUNT(*) = 0 from TwinFieldSimpleNonIndexedEntity tfs
        inner join TwinEntity t on tfs.twinId = t.id
        where t.ownerUserId = :ownerUserId and tfs.value = :value and tfs.twinClassFieldId = :twinClassFieldId
    """)
    boolean existsByTwinClassFieldIdAndValueAndOwnerUserId(UUID twinClassFieldId, String value, UUID ownerUserId);

    @Query(value = """
        select COUNT(*) = 0 from TwinFieldSimpleNonIndexedEntity tfs
        inner join TwinEntity t on tfs.twinId = t.id
        where t.ownerBusinessAccountId = :ownerBusinessAccountId and tfs.value = :value and tfs.twinClassFieldId = :twinClassFieldId
    """)
    boolean existsByTwinClassFieldIdAndValueAndOwnerBusinessAccountId(UUID twinClassFieldId, String value, UUID ownerBusinessAccountId);
}
