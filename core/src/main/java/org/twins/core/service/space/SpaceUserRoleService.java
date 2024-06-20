package org.twins.core.service.space;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.domain.space.UsersRefSpaceRolePageable;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.cambium.common.util.PaginationUtils.paginationOffset;
import static org.cambium.common.util.PaginationUtils.sort;
import static org.springframework.data.jpa.domain.Specification.where;
import static org.twins.core.dao.specifications.space.SpaceRoleUserSpecification.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserRoleService {
    final EntitySmartService entitySmartService;
    final SpaceRoleUserRepository spaceRoleUserRepository;
    final AuthService authService;
    final TwinService twinService;

    // twinId is equivalent of spaceId

    public UsersRefSpaceRolePageable getAllUsersRefRolesBySpaceIdMap(UUID twinId, int offset, int limit) throws ServiceException {
        return getAllUsersRefRolesBySpaceIdMap(twinService.findEntitySafe(twinId), offset, limit);
    }

    public UsersRefSpaceRolePageable getAllUsersRefRolesBySpaceIdMap(TwinEntity twinEntity, int offset, int limit) throws ServiceException {
        Page<SpaceRoleUserEntity> spaceRoleUserEntities = spaceRoleUserRepository.findAll(
                where(checkUuid(SpaceRoleUserEntity.Fields.twinId, twinEntity.getId(), false)),
                paginationOffset(offset, limit, sort(false, TwinEntity.Fields.createdAt))
        );
        return createUserRoleMap(spaceRoleUserEntities, offset, limit);
    }


    public UsersRefSpaceRolePageable getUsersRefRolesMap(SpaceRoleUserSearch search, UUID twinId, int offset, int limit) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        Pageable pageable = paginationOffset(offset, limit, sort(false, TwinEntity.Fields.createdAt));
        Specification<SpaceRoleUserEntity> spec = where(
                checkUuid(SpaceRoleUserEntity.Fields.twinId, twinEntity.getId(), false)
                .and(checkUserNameLikeWithPattern(search.getUserNameLike()))
                .and(checkUuidIn(SpaceRoleUserEntity.Fields.spaceRoleId, search.getSpaceRolesIdList(), false))
                .and(checkUserInGroups(search.getUserGroupIdList(), false))
        );
        Page<SpaceRoleUserEntity> spaceRoleUserEntities = spaceRoleUserRepository.findAll(spec, pageable);
        return createUserRoleMap(spaceRoleUserEntities, offset, limit);
    }

    private UsersRefSpaceRolePageable createUserRoleMap(Page<SpaceRoleUserEntity> spaceRoleUserEntities, int offset, int limit) {
        List<UserRefSpaceRole> resultList = new ArrayList<>();
        Map<UserEntity, List<SpaceRoleUserEntity>> map = new HashMap<>();
        for(SpaceRoleUserEntity item : spaceRoleUserEntities) {
            map.putIfAbsent(item.getUser(), new ArrayList<>());
            map.get(item.getUser()).add(item);
        }
        for(var entry : map.entrySet()) resultList.add(new UserRefSpaceRole().setUser(entry.getKey()).addRoles(entry.getValue()));
        return (UsersRefSpaceRolePageable) new UsersRefSpaceRolePageable()
                .setUsersRefRoles(resultList)
                .setOffset(offset)
                .setLimit(limit)
                .setTotal(spaceRoleUserEntities.getTotalElements());
    }

    public List<UserEntity> findUserByRole(UUID twinId, UUID spaceRoleId) throws ServiceException {
        return spaceRoleUserRepository.findByTwinIdAndSpaceRoleId(twinId, spaceRoleId);
    }

    public List<SpaceRoleUserEntity> findSpaceRoleUsersByTwinIdAndUserId(UUID twinId, UUID userId) throws ServiceException {
        return spaceRoleUserRepository.findAllByTwinIdAndUserId(twinId, userId);
    }

    @Transactional
    public void manageSpaceRoleForUsers(UUID spaceId, UUID roleId, List<UUID> spaceRoleUserEnterList, List<UUID> spaceRoleUserExitList) throws ServiceException {
        UUID createUserId = authService.getApiUser().getUser().getId();
        addEntryRoleUserList(spaceId, roleId, createUserId, spaceRoleUserEnterList);
        deleteEntryRoleUserList(spaceId, roleId, spaceRoleUserExitList);
    }

    private void addEntryRoleUserList(UUID spaceId, UUID roleId, UUID createUserId, List<UUID> spaceRoleUserEnterList) {
        if (CollectionUtils.isEmpty(spaceRoleUserEnterList))
            return;
        List<SpaceRoleUserEntity> list = new ArrayList<>();
        for (UUID userId : spaceRoleUserEnterList) {
            if (checkSeemEntityInDB(spaceId, roleId, userId)) {
                log.warn("user[" + userId + "] is already registered for role[" + roleId + "] in space[" + spaceId + "]");
                continue;
            }
            list.add(new SpaceRoleUserEntity()
                    .setTwinId(spaceId)
                    .setSpaceRoleId(roleId)
                    .setUserId(userId)
                    .setCreatedByUserId(createUserId)
                    .setCreatedAt(Timestamp.valueOf(LocalDateTime.now()))
            );
            if (CollectionUtils.isNotEmpty(list))
                entitySmartService.saveAllAndLog(list, spaceRoleUserRepository);
        }
    }

    private void deleteEntryRoleUserList(UUID spaceId, UUID roleId, List<UUID> spaceRoleUserExitList) {
        if (CollectionUtils.isNotEmpty(spaceRoleUserExitList)) {
            for (UUID userId : spaceRoleUserExitList) {
                spaceRoleUserRepository.deleteAllByTwinIdAndSpaceRoleIdAndUserId(spaceId, roleId, userId);
                log.info("user[" + userId + "] perhaps was deleted by space[" + spaceId + "] and role[" + roleId + "]");
            }
        }
    }

    private Boolean checkSeemEntityInDB(UUID spaceId, UUID roleId, UUID userId) {
        return spaceRoleUserRepository.existsByTwinIdAndSpaceRoleIdAndUserId(spaceId, roleId, userId);
    }
}
