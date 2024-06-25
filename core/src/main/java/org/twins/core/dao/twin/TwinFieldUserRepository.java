package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinFieldUserRepository extends CrudRepository<TwinFieldUserEntity, UUID>, JpaSpecificationExecutor<TwinFieldUserEntity> {
    List<TwinFieldUserEntity> findByTwinId(UUID twinId);

    List<TwinFieldUserEntity> findByTwinIdIn(Set<UUID> twinIds);

    @Query(value = """
            select distinct field.twinClassFieldId
            from TwinFieldUserEntity field where field.twin.twinClassId = :twinClassId and field.twinClassFieldId in (:twinClassFields)
            """)
    List<UUID> findUsedFieldsByTwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);

    void deleteByTwin_TwinClassIdAndTwinClassFieldIdIn(@Param("twinClassId") UUID twinClassId, @Param("twinClassFields") Collection<UUID> twinClassFields);
}
