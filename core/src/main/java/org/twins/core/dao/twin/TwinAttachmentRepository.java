package org.twins.core.dao.twin;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinAttachmentRepository extends CrudRepository<TwinAttachmentEntity, UUID>, JpaSpecificationExecutor<TwinAttachmentEntity> {
    TwinAttachmentEntity getById(UUID twinId);
    List<TwinAttachmentEntity> findByTwinId(UUID twinId);
    List<TwinAttachmentEntity> findByTwinCommentId(UUID twinId);
    List<TwinAttachmentEntity> findByTwinIdIn(Collection<UUID> twinIdList);
    List<TwinAttachmentEntity> findByTwinCommentIdIn(Collection<UUID> twinCommentIdList);

    List<TwinAttachmentEntity> findByTwinIdAndIdIn(UUID twinId, List<UUID> idList);

    void deleteAllByTwinIdAndIdIn(UUID twinId, List<UUID> idList);

    @Query(value = """
        SELECT 
            twin.twinId,
            COUNT(CASE 
                WHEN twin.twinCommentId IS NULL 
                AND twin.twinClassFieldId IS NULL 
                AND twin.twinflowTransitionId IS NULL 
                THEN 1 ELSE NULL END) AS directCount,
            COUNT(CASE 
                WHEN twin.twinCommentId IS NOT NULL 
                THEN 1 ELSE NULL END) AS commentCount,
            COUNT(CASE 
                WHEN twin.twinflowTransitionId IS NOT NULL 
                THEN 1 ELSE NULL END) AS transitionCount,
            COUNT(CASE 
                WHEN twin.twinClassFieldId IS NOT NULL 
                THEN 1 ELSE NULL END) AS fieldCount
        FROM TwinAttachmentEntity twin
        WHERE twin.twinId IN :twinIds
        GROUP BY twin.twinId
        """)
    List<Object[]> countAttachmentsByTwinIds(@Param("twinIds") List<UUID> twinIds);

    @Query("SELECT twin.twinId, COUNT(twin) FROM TwinAttachmentEntity twin WHERE twin.twinId IN :twinIds GROUP BY twin.twinId")
    List<Object[]> countByTwinIds(@Param("twinIds") List<UUID> twinIds);

}
