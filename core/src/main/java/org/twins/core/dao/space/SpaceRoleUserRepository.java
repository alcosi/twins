package org.twins.core.dao.space;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SpaceRoleUserRepository extends CrudRepository<SpaceRoleUserEntity, UUID>, JpaSpecificationExecutor<SpaceRoleUserEntity> {

    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId = :spaceRoleId")
    List<UserEntity> findByTwinIdAndSpaceRoleId(@Param("twinId") UUID twinId, @Param("spaceRoleId") UUID spaceRoleId);

    List<SpaceRoleUserEntity> findAllByTwinIdAndUserId(UUID twinId, UUID userId);

    @Query("SELECT sru.userId FROM SpaceRoleUserEntity sru WHERE sru.twinId = :spaceTwinId AND sru.spaceRoleId = :spaceRoleId AND sru.userId IN :uuidSet")
    Set<UUID> findUserIdsByTwinIdAndSpaceRoleIdAndUserIdIn(@Param("spaceTwinId") UUID spaceTwinId, @Param("spaceRoleId") UUID spaceRoleId, @Param("uuidSet") Set<UUID> uuidSet);

    @Transactional
    void deleteAllByTwinIdAndSpaceRoleIdAndUserIdIn(UUID spaceTwinId, UUID spaceRoleId, Set<UUID> userIds);

}
