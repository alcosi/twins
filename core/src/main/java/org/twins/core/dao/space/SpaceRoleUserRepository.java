package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRoleUserRepository extends CrudRepository<SpaceRoleUserEntity, UUID>, JpaSpecificationExecutor<SpaceRoleUserEntity> {
    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId = :spaceRoleId")
    List<UserEntity> findByTwinIdAndSpaceRoleId(@Param("twinId") UUID twinId, @Param("spaceRoleId") UUID spaceRoleId);

    void deleteAllByUserId(UUID id);

    @Query(value = "SELECT CASE WHEN COUNT(sru) > 0 THEN true ELSE false END FROM SpaceRoleUserEntity sru " +
            "WHERE sru.twinId = :twinId AND sru.spaceRoleId = :roleId AND sru.userId = :userId")
    boolean existsByTwinIdAndRoleIdAndUserId(UUID twinId, UUID roleId, UUID userId);
}
