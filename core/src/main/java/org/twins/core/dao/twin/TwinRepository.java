package org.twins.core.dao.twin;

import org.hibernate.query.TypedParameterValue;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface TwinRepository extends CrudRepository<TwinEntity, UUID>, JpaSpecificationExecutor<TwinEntity>, PagingAndSortingRepository<TwinEntity, UUID> {
    List<TwinEntity> findByTwinClassDomainId(UUID domainId);

    List<TwinEntity> findByOwnerBusinessAccountId(UUID businessAccount);

    List<TwinEntity> findByTwinClassId(UUID twinClassId);

    @Query(value = "select t.assignerUser from TwinEntity t where t.id = :twinId")
    UserEntity getAssignee(@Param("twinId") UUID twinId);

    @Modifying
    @Query("delete from TwinEntity te where te.ownerBusinessAccountId = :businessAccountId and te.twinClass.domainId = :domainId")
    int deleteAllByBusinessAccountIdAndDomainId(UUID businessAccountId, UUID domainId);

    @Query(value = "select function('permissionCheck', :domainId, :businessAccountId, :permissionSpaceId, :permissionId, :userId, :userGroupId, :twinClassId, :isAssignee, :isCreator)")
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
}
