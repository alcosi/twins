package org.twins.core.dao.space;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "select su from SpaceRoleUserEntity su where su.twinId = :twinId")
    Page<SpaceRoleUserEntity> findAllByTwinId(@Param("twinId") UUID twinId, Pageable pageable);

    @Query(value = "select su.user from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId = :spaceRoleId")
    List<UserEntity> findByTwinIdAndSpaceRoleId(@Param("twinId") UUID twinId, @Param("spaceRoleId") UUID spaceRoleId);

    @Query(value = "select su from SpaceRoleUserEntity su where su.twinId = :twinId and lower(su.user.name) like CONCAT('%', lower(:nameLike), '%')")
    Page<SpaceRoleUserEntity> findByTwinIdAndNameLike(@Param("twinId") UUID twinId, @Param("nameLike") String nameLike, Pageable pageable);

    @Query(value = "select su from SpaceRoleUserEntity su where su.twinId = :twinId and su.spaceRoleId in :spaceRoleIds")
    Page<SpaceRoleUserEntity> findByTwinIdAndRoleIn(@Param("twinId") UUID twinId, @Param("spaceRoleIds") Collection<UUID> spaceRoleIds, Pageable pageable);

    @Query(value = "select su from SpaceRoleUserEntity su where su.twinId = :twinId and lower(su.user.name) like CONCAT('%', lower(:nameLike), '%') and su.spaceRoleId in :spaceRoleIds")
    Page<SpaceRoleUserEntity> findByTwinIdAndNameLikeAndRoleIn(@Param("twinId") UUID twinId, @Param("nameLike") String nameLike, @Param("spaceRoleIds") Collection<UUID> spaceRoleIds, Pageable pageable);

    void deleteAllByTwinIdAndSpaceRoleIdAndUserId(UUID twinId, UUID spaceRoleId, UUID userId);

    boolean existsByTwinIdAndSpaceRoleIdAndUserId(UUID twinId, UUID roleId, UUID userId);
}
