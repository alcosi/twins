package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SpaceRoleUserRepository extends CrudRepository<SpaceRoleUserEntity, UUID>, JpaSpecificationExecutor<SpaceRoleUserEntity> {

    @Query(value = "select su.userSpecOnly from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId = :spaceRoleId")
    List<UserEntity> findByTwinIdAndSpaceRoleId(@Param("twinId") UUID twinId, @Param("spaceRoleId") UUID spaceRoleId);

    List<SpaceRoleUserEntity> findAllByTwinIdAndUserId(UUID twinId, UUID userId);
    List<SpaceRoleUserEntity> findAllByTwinIdAndSpaceRoleId(UUID spaceId, UUID spaceRoleId);
    List<SpaceRoleUserEntity> findAllByTwinIdAndSpaceRoleIdAndUserId(UUID spaceId, UUID spaceRoleId, UUID userId);
    List<SpaceRoleUserEntity> findAllByTwinIdInAndSpaceRoleIdAndUserId(Collection<UUID> spaceIds, UUID spaceRoleId, UUID userId);
    List<SpaceRoleUserEntity> findAllByTwinIdAndSpaceRoleIdAndUserIdIn(UUID spaceId, UUID spaceRoleId, Collection<UUID> userIds);

    @Modifying
    @Query("DELETE FROM SpaceRoleUserEntity sru WHERE sru.spaceRoleId = :roleId AND sru.twinId = :spaceId AND sru.userId IN :userIds")
    void deleteBySpaceIdAndSpaceRoleIdAndUserIdIn(@Param("spaceId") UUID spaceId, @Param("roleId") UUID roleId, @Param("userIds") Collection<UUID> userIds);

    List<SpaceRoleUserEntity> findByTwinIdIn(Set<UUID> spaceSet);

    @Query("select distinct sru.userId from SpaceRoleUserEntity sru where sru.twinId = :twinId and sru.spaceRoleId in :spaceRoleIds")
    Set<UUID> findUserIdsByTwinIdAndSpaceRoleIds(UUID twinId, Collection<UUID> spaceRoleIds);

    /**
     * Users joined to the given twin's space, capped to {@code limit} rows
     * (safety valve for large spaces; pass limit+1 to detect overflow).
     */
    @Query(value = "select distinct user_id from space_role_user where twin_id = :twinId limit :limit", nativeQuery = true)
    Set<UUID> findUserIdsByTwinId(@Param("twinId") UUID twinId, @Param("limit") int limit);

    /**
     * Bulk variant of {@link #findUserIdsByTwinIdAndSpaceRoleIds}: resolves users across many twins (spaces)
     * sharing the same space roles in a single query, returning {@code (twinId, userId)} pairs to group by
     * twin. Used by batch recipient resolvers where a whole chunk shares the same resolver params
     * (fixed spaceRoleIds) and only twin ids vary per history.
     */
    @Query("select sru.twinId, sru.userId from SpaceRoleUserEntity sru where sru.twinId in :twinIds and sru.spaceRoleId in :spaceRoleIds")
    List<Object[]> findUserIdsByTwinIdInAndSpaceRoleIdsIn(@Param("twinIds") Collection<UUID> twinIds, @Param("spaceRoleIds") Collection<UUID> spaceRoleIds);
}
