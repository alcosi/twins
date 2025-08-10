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

public interface TwinFieldTwinClassListRepository extends CrudRepository<TwinFieldTwinClassEntity, UUID>, JpaSpecificationExecutor<TwinFieldTwinClassEntity> {

    List<TwinFieldTwinClassEntity> findByTwinId(UUID twinId);
    boolean existsByTwinClassFieldId(UUID twinClassFieldId);
    List<TwinFieldTwinClassEntity> findByTwinIdIn(Set<UUID> twinIds);

    @Query(value = """
            select distinct field.twinClassFieldId
            from TwinFieldTwinClassEntity field where field.twin.twinClassId = :twinClassId and field.twinClassFieldId in (:twinClassFields)
            """)
    List<UUID> findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    void deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    @Transactional
    @Modifying
    @Query(value = "update TwinFieldTwinClassEntity set twinClassFieldId = :toTwinClassFieldId where twinClassFieldId = :fromTwinClassFieldId and twin.twinClassId = :twinClassId")
    void replaceTwinClassFieldForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("fromTwinClassFieldId") UUID fromTwinClassFieldId, @Param("toTwinClassFieldId") UUID toTwinClassFieldId);

    void deleteByTwinIdAndTwinClassFieldIdIn(UUID twinId, Set<UUID> twinClassFieldIds);

}
