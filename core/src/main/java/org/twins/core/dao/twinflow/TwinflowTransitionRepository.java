package org.twins.core.dao.twinflow;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.enums.twinflow.TwinflowTransitionType;

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
    @Query(value = "select tt from TwinflowTransitionEntity tt where tt.twinflowId = :twinflowId and (tt.srcTwinStatusId = :srcTwinStatusId or (tt.srcTwinStatusId is null and tt.dstTwinStatusId != :srcTwinStatusId)) " +
            " and true = function('permission_check_mater', :permissionSchemaId, tt.permissionId, :permissionSpaceId, :userId, :userGroupFootprintId, :twinClassId, :isCreator, :isAssignee)")
    List<TwinflowTransitionEntity> findValidTransitions(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("permissionSchemaId") UUID permissionSchemaId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupFootprintId") UUID userGroupFootprintId,
            @Param("twinClassId") UUID twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    @Query(value = "select tt from TwinflowTransitionEntity tt where tt.id = :transitionId " +
            " and true = function('permission_check_mater', :permissionSchemaId, tt.permissionId, :permissionSpaceId, :userId, :userGroupFootprintId, :twinClassId, :isCreator, :isAssignee)")
    TwinflowTransitionEntity findTransition(
            @Param("transitionId") UUID transitionId,
            @Param("permissionSchemaId") UUID permissionSchemaId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupFootprintId") UUID userGroupFootprintId,
            @Param("twinClassId") UUID twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    @Query(value = "select tt from TwinflowTransitionEntity tt " +
            "where tt.twinflowId = :twinflowId and (tt.srcTwinStatusId = :srcTwinStatusId or (tt.srcTwinStatusId is null and tt.dstTwinStatusId != :srcTwinStatusId)) " +
            "and tt.twinflowTransitionAlias.alias = :alias " +
            "and tt.twinflowTransitionTypeId != :twinflowTransitionType " +
            "and true = function('permission_check_mater', :permissionSchemaId, tt.permissionId, :permissionSpaceId, :userId, :userGroupFootprintId, :twinClassId, :isCreator, :isAssignee)")
    TwinflowTransitionEntity findTransitionByAlias(
            @Param("twinflowId") UUID twinflowId,
            @Param("srcTwinStatusId") UUID srcTwinStatusId,
            @Param("alias") String alias,
            @Param("twinflowTransitionType") TwinflowTransitionType twinflowTransitionType,
            @Param("permissionSchemaId") UUID permissionSchemaId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupFootprintId") UUID userGroupFootprintId,
            @Param("twinClassId") UUID twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    List<TwinflowTransitionEntity> findByTwinflowId(UUID twinflowId);

    List<TwinflowTransitionEntity> findByTwinflowIdIn(Collection<UUID> twinflowIds);

    @Query("SELECT t.inbuiltTwinFactoryId, COUNT(t) FROM TwinflowTransitionEntity t WHERE t.inbuiltTwinFactoryId IN :ids GROUP BY t.inbuiltTwinFactoryId")
    List<Object[]> countByInbuiltTwinFactoryIds(Collection<UUID> ids);

    @Query("SELECT t.twinflowTransitionAliasId, COUNT(t) FROM TwinflowTransitionEntity t WHERE t.twinflowTransitionAliasId IN :ids GROUP BY t.twinflowTransitionAliasId")
    List<Object[]> countByTransitionAliasIds(Collection<UUID> ids);
}
