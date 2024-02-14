package org.twins.core.dao.space;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twins.core.dao.user.UserEntity;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface SpaceRoleUserRepository extends CrudRepository<SpaceRoleUserEntity, UUID>, JpaSpecificationExecutor<SpaceRoleUserEntity> {

//    I wrote without specifications, because it is not clear what is more effective
//    to take a user with joins or a space-role-user...
    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId")
    List<UserEntity> findAllByTwinId(@Param("twinId") UUID twinId);

    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId = :spaceRoleId")
    List<UserEntity> findByTwinIdAndSpaceRoleId(@Param("twinId") UUID twinId, @Param("spaceRoleId") UUID spaceRoleId);

    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and lower(su.user.name) like CONCAT('%', :nameLike, '%')")
    List<UserEntity> findByTwinIdAndNameLike(@Param("twinId") UUID twinId, @Param("nameLike") String nameLike);

    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and lower(su.user.name) like CONCAT('%', :nameLike, '%') and su.spaceRoleId in :spaceRoleIds")
    List<UserEntity> findByTwinIdAndNameLikeAndRoleIn(@Param("twinId") UUID twinId, @Param("nameLike") String nameLike, @Param("spaceRoleIds") Collection<UUID> spaceRoleIds);

    void deleteAllByTwinIdAndSpaceRoleIdAndUserId(UUID twinId, UUID spaceRoleId, UUID userId);

    boolean existsByTwinIdAndSpaceRoleIdAndUserId(UUID twinId, UUID roleId, UUID userId);
}
