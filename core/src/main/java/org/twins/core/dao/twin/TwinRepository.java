package org.twins.core.dao.twin;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TwinRepository extends CrudRepository<TwinEntity, UUID>, JpaSpecificationExecutor<TwinEntity>, PagingAndSortingRepository<TwinEntity, UUID> {
    List<TwinEntity> findByTwinClassDomainId(UUID domainId);

    List<TwinEntity> findByOwnerBusinessAccountId(UUID businessAccount);

    List<TwinEntity> findByTwinClassId(UUID twinClassId);

    boolean existsByTwinClassId(UUID twinClassId);

    @Query(value = "select t.assignerUser from TwinEntity t where t.id = :twinId")
    UserEntity getAssignee(@Param("twinId") UUID twinId);

    @Modifying
    @Query("delete from TwinEntity te where te.ownerBusinessAccountId = :businessAccountId and te.twinClass.domainId = :domainId")
    int deleteAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

    @Query(value = "select function('permission_check', :domainId, :businessAccountId, :permissionSpaceId, :permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator)")
    boolean hasPermission(
            @Param("permissionId") UUID permissionId,
            @Param("domainId") UUID domainId,
            @Param("businessAccountId") TypedParameterValue<UUID> businessAccountId,
            @Param("permissionSpaceId") TypedParameterValue<UUID> permissionSpaceId,
            @Param("userId") UUID userId,
            @Param("userGroupId") TypedParameterValue<UUID[]> userGroupIds,
            @Param("twinClassId") TypedParameterValue<UUID> twinClassId,
            @Param("isAssignee") boolean isAssignee,
            @Param("isCreator") boolean isCreator);

    @Query(value = "select distinct t.headTwinId from TwinEntity t where t.twinClassId = :twinClassId and t.headTwinId is not null")
    Set<UUID> findDistinctHeadTwinIdByTwinClassId(UUID twinClassId);

    @Transactional
    @Modifying
    @Query(value = "update TwinEntity set headTwinId = :newVal where headTwinId = :oldVal and twinClassId = :twinClassId")
    void replaceHeadTwinForTwinsOfClass(@Param("twinClassId") UUID twinClassId, @Param("oldVal") UUID oldVal, @Param("newVal") UUID newVal);

    @Query(value = "select t.id from TwinEntity t where t.twinClassId = :twinClassId")
    Set<UUID> findIdByTwinClassId(@Param("twinClassId") UUID twinClassId);

    @Query(value = "SELECT create_permission_id_detect(:headTwinId, :businessAccountId, :domainId, :twinClassId)")
    UUID detectCreatePermissionId(
            @Param("headTwinId") TypedParameterValue<UUID> headTwinId,
            @Param("businessAccountId") TypedParameterValue<UUID> domainBusinessAccountId,
            @Param("domainId") TypedParameterValue<UUID> domainId,
            @Param("twinClassId") TypedParameterValue<UUID> twinClassId);

}
