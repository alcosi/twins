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
    @Query(value = "select tt from TwinflowTransitionEntity tt where tt.twinflowId = :twinflowId and tt.srcTwinStatusId = :srcTwinStatusId " +
            " and true = function('permissionCheck', :domainId, :businessAccountId, :permissionSpaceId, tt.permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator)")
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

    @Query(value = "select tt from TwinflowTransitionEntity tt where tt.twinflowId = :twinflowId and tt.srcTwinStatusId = :srcTwinStatusId and tt.twinflowTransitionAliasId = :aliasId " +
            " and true = function('permissionCheck', :domainId, :businessAccountId, :permissionSpaceId, tt.permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator)")
    TwinflowTransitionEntity findTransitionByAlias(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("aliasId") String alias,
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
