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

    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId = :spaceRoleId")
    List<UserEntity> findByTwinIdAndSpaceRoleId(@Param("twinId") UUID twinId, @Param("spaceRoleId") UUID spaceRoleId);

    List<SpaceRoleUserEntity> findAllByTwinIdAndUserId(UUID twinId, UUID userId);
    List<SpaceRoleUserEntity> findAllByTwinIdAndSpaceRoleId(UUID spaceId, UUID spaceRoleId);
    List<SpaceRoleUserEntity> findAllByTwinIdAndSpaceRoleIdAndUserId(UUID spaceId, UUID spaceRoleId, UUID userId);
    List<SpaceRoleUserEntity> findAllByTwinIdAndSpaceRoleIdAndUserIdIn(UUID spaceId, UUID spaceRoleId, Collection<UUID> userIds);

    @Modifying
    @Query("DELETE FROM SpaceRoleUserEntity sru WHERE sru.spaceRoleId = :roleId AND sru.twinId = :spaceId AND sru.userId IN :userIds")
    void deleteBySpaceIdAndSpaceRoleIdAndUserIdIn(@Param("spaceId") UUID spaceId, @Param("roleId") UUID roleId, @Param("userIds") Collection<UUID> userIds);

    List<SpaceRoleUserEntity> findByTwinIdIn(Set<UUID> spaceSet);

    @Query("select distinct sru.userId from SpaceRoleUserEntity sru where sru.twinId = :twinId and sru.spaceRoleId in :spaceRoleIds")
    Set<UUID> findUserIdsByTwinIdAndSpaceRoleIds(UUID twinId, Collection<UUID> spaceRoleIds);
}
