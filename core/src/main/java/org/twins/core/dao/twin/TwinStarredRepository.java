package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinStarredRepository extends CrudRepository<TwinStarredEntity, UUID> {
    void deleteByTwinIdAndUserId(UUID twinId, UUID userId);

    TwinStarredEntity findByTwinIdAndUserId(UUID twinId, UUID userId);

    @Query(value = "select twinStarred from TwinStarredEntity twinStarred join TwinEntity twin on twin.id = twinStarred.twinId where twin.twinClassId = :twinClassId")
    List<TwinStarredEntity> findTwinStarredListByTwinClassId(@Param("twinClassId") UUID twinClassId);

}
