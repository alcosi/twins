package org.twins.core.dao.twinflow;

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
public interface TwinflowTransitionRepository extends CrudRepository<TwinflowTransitionEntity, UUID>, JpaSpecificationExecutor<TwinflowTransitionEntity> {
    @Query(value = "from TwinflowTransitionEntity where twinflowId = :twinflowId and srcTwinStatusId = :srcTwinStatusId " +
            " and function('permissionCheck', :domainId, :businessAccountId, permissionId, :permissionSpaceId, :userId, :userGroupId, :isAssignee, :isCreator, :twinClassId)")
    List<TwinflowTransitionEntity> findValidTransitions(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") UUID businessAccountId,
            @Param("permissionSpaceId") UUID permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") Set<UUID> userGroupIds,
            @Param("isAssignee") Boolean isAssignee,
            @Param("isCreator") Boolean isCreator,
            @Param("twinClassId") UUID twinClassId);

    @Query(value = "from TwinflowTransitionEntity where id = :transitionId " +
            " and function('permissionCheck', :domainId, :businessAccountId, permissionId, :permissionSpaceId, :userId, :userGroupId, :isAssignee, :isCreator, :twinClassId)")
    TwinflowTransitionEntity findTransition(
            @Param("transitionId") UUID transitionId,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") UUID businessAccountId,
            @Param("permissionSpaceId") UUID permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") Set<UUID> userGroupIds,
            @Param("isAssignee") Boolean isAssignee,
            @Param("isCreator") Boolean isCreator,
            @Param("twinClassId") UUID twinClassId);

    @Query(value = "from TwinflowTransitionEntity where twinflowId = :twinflowId and srcTwinStatusId = :srcTwinStatusId and twinflowTransitionAliasId = :aliasId " +
            " and function('permissionCheck', :domainId, :businessAccountId, permissionId, :permissionSpaceId, :userId, :userGroupId, :isAssignee, :isCreator, :twinClassId)")
    TwinflowTransitionEntity findTransitionByAlias(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("aliasId") String alias,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") UUID businessAccountId,
            @Param("permissionSpaceId") UUID permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") Set<UUID> userGroupIds,
            @Param("isAssignee") Boolean isAssignee,
            @Param("isCreator") Boolean isCreator,
            @Param("twinClassId") UUID twinClassId);

    List<TwinflowTransitionEntity> findByTwinflowId(UUID twinflowId);
    List<TwinflowTransitionEntity> findByTwinflowIdIn(Collection<UUID> twinflowIds);
}
