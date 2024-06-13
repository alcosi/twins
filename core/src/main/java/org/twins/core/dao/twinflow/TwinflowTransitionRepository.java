package org.twins.core.dao.twinflow;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface TwinflowTransitionRepository extends CrudRepository<TwinflowTransitionEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionEntity> {
    /* we can have 2 concurrent transitions to same dst_status_id:
        1. with src_status_id = null - case of "from any" transition
        2. with specific src_status_id
        Second case has more priority
        This logic can be done with postgres sql "distinct on" operator, but it's not supported in hibernate
        */
    @Query(value = "select tt from TwinflowTransitionEntity tt where tt.twinflowId = :twinflowId and (tt.srcTwinStatusId = :srcTwinStatusId or tt.srcTwinStatusId is null) " +
            " and true = function('permissionCheck', :domainId, :businessAccountId, :permissionSpaceId, tt.permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator) order by tt.dstTwinStatusId, tt.srcTwinStatusId")
    List<TwinflowTransitionEntity> findValidTransitions(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") TypedParameterValue<UUID> businessAccountId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") TypedParameterValue<UUID[]> userGroupIds,
            @Param("twinClassId") TypedParameterValue<UUID> twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    @Query(value = "select tt from TwinflowTransitionEntity tt where tt.id = :transitionId " +
            " and true = function('permissionCheck', :domainId, :businessAccountId, :permissionSpaceId, tt.permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator)")
    TwinflowTransitionEntity findTransition(
            @Param("transitionId") UUID transitionId,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") TypedParameterValue<UUID> businessAccountId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") TypedParameterValue<UUID[]> userGroupIds,
            @Param("twinClassId") TypedParameterValue<UUID> twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    @Query(value = "select tt from TwinflowTransitionEntity tt " +
            "where tt.twinflowId = :twinflowId and (tt.srcTwinStatusId = :srcTwinStatusId or tt.srcTwinStatusId is null) " +
            "and tt.twinflowTransitionAlias.alias = :alias " +
            "and true = function('permissionCheck', :domainId, :businessAccountId, :permissionSpaceId, tt.permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator) order by tt.srcTwinStatusId limit 1")
    TwinflowTransitionEntity findTransitionByAlias(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("alias") String alias,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") TypedParameterValue<UUID> businessAccountId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") TypedParameterValue<UUID[]> userGroupIds,
            @Param("twinClassId") TypedParameterValue<UUID> twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    List<TwinflowTransitionEntity> findByTwinflowId(UUID twinflowId);

    List<TwinflowTransitionEntity> findByTwinflowIdIn(Collection<UUID> twinflowIds);
}
