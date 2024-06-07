package org.twins.core.dao.twin;

import org.hibernate.query.TypedParameterValue;
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

    @Query(value = "select ts from TwinStarredEntity ts join TwinEntity t on t.id = ts.twinId where t.twinClassId = :twinClassId " +
            "and ts.userId = :userId " +
            "and true = function('permissionCheck', :domainId, :businessAccountId, t.permissionSchemaSpaceId, t.viewPermissionId, t.twinClass.viewPermissionId, :userId, :userGroupIds, t.twinClassId, (t.assignerUserId = :userId), (t.createdByUserId = :userId))")
    List<TwinStarredEntity> findTwinStarredListByTwinClassIdAndUserIdAndUserGroupId(
            @Param("twinClassId") UUID twinClassId,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") TypedParameterValue<UUID> businessAccountId,
            @Param("userId") UUID userId,
            @Param("userGroupIds") TypedParameterValue<UUID[]> userGroupIds);

}
