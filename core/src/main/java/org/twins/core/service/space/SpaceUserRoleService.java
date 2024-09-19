package org.twins.core.service.space;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.PaginationUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.service.auth.AuthService;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specification.allOf;
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
    final UserService userService;

    // twinId is equivalent of spaceId

    public PaginationResult<UserRefSpaceRole> getAllUsersRefRolesBySpaceIdMap(UUID twinId, SimplePagination pagination) throws ServiceException {
        return getAllUsersRefRolesBySpaceIdMap(twinService.findEntitySafe(twinId), pagination);
    }

    public PaginationResult<UserRefSpaceRole> getAllUsersRefRolesBySpaceIdMap(TwinEntity twinEntity, SimplePagination pagination) throws ServiceException {
        Page<SpaceRoleUserEntity> spaceRoleUserEntities = spaceRoleUserRepository.findAll(
                where(checkUuid(SpaceRoleUserEntity.Fields.twinId, twinEntity.getId(), false)), PaginationUtils.pageableOffset(pagination)
        );
        return createUserRoleMap(spaceRoleUserEntities, pagination);
    }


    public PaginationResult<UserRefSpaceRole> getUsersRefRolesMap(SpaceRoleUserSearch search, UUID twinId, SimplePagination pagination) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        Specification<SpaceRoleUserEntity> spec = where(
                checkUuid(SpaceRoleUserEntity.Fields.twinId, twinEntity.getId(), false)
                        .and(checkUserNameLikeWithPattern(search.getUserNameLike()))
                        .and(checkUuidIn(SpaceRoleUserEntity.Fields.spaceRoleId, search.getSpaceRolesIdList(), false))
                        .and(checkUserInGroups(search.getUserGroupIdList(), false))
        );
        Page<SpaceRoleUserEntity> spaceRoleUserEntities = spaceRoleUserRepository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return createUserRoleMap(spaceRoleUserEntities, pagination);
    }

    private PaginationResult<UserRefSpaceRole> createUserRoleMap(Page<SpaceRoleUserEntity> spaceRoleUserEntities, SimplePagination pagination) throws ServiceException {
        List<UserRefSpaceRole> resultList = new ArrayList<>();
        Map<UserEntity, List<SpaceRoleUserEntity>> map = new HashMap<>();
        for(SpaceRoleUserEntity item : spaceRoleUserEntities) {
            map.putIfAbsent(item.getUser(), new ArrayList<>());
            map.get(item.getUser()).add(item);
        }
        for(var entry : map.entrySet()) resultList.add(new UserRefSpaceRole().setUser(entry.getKey()).addRoles(entry.getValue()));
        return PaginationUtils.convertInPaginationResult(resultList, pagination, spaceRoleUserEntities.getTotalElements());
    }

    public List<UserEntity> findUserBySpaceIdAndRoleId(UUID spaceId, UUID roleId) throws ServiceException {
        return spaceRoleUserRepository.findByTwinIdAndSpaceRoleId(spaceId, roleId);
    }

    public List<SpaceRoleUserEntity> findSpaceRoleUsersByTwinIdAndUserId(UUID twinId, UUID userId) throws ServiceException {
        return spaceRoleUserRepository.findAllByTwinIdAndUserId(twinId, userId);
    }

    @Transactional
    public void manageSpaceRoleForUsers(UUID spaceId, UUID roleId, List<UUID> enterList, List<UUID> exitList) throws ServiceException {
        List<UUID> existingUserList = loadExistingUsers(spaceId, roleId);
        addNewUsers(spaceId, roleId, enterList, existingUserList);
        removeExtraUsers(spaceId, roleId, new ArrayList<>(), exitList);
    }

    @Transactional
    public void overrideSpaceRoleUsers(UUID spaceId, UUID roleId, List<UUID> overrideList) throws ServiceException {
        if (CollectionUtils.isEmpty(overrideList))
            return;
        List<UUID> existingUsers = loadExistingUsers(spaceId, roleId);
        addNewUsers(spaceId, roleId, overrideList, existingUsers);
        removeExtraUsers(spaceId, roleId, overrideList, existingUsers);
    }

    private List<UUID> loadExistingUsers(UUID spaceId, UUID roleId) {
        return spaceRoleUserRepository.findAllByTwinIdAndSpaceRoleId(spaceId, roleId)
                .stream()
                .map(SpaceRoleUserEntity::getUserId)
                .toList();
    }

    private void addNewUsers(UUID spaceId, UUID roleId, List<UUID> overrideList, List<UUID> existingUsers) throws ServiceException {
        ApiUser currectApiUser = authService.getApiUser();
        List<UUID> userToAddList = overrideList.stream()
                .filter(userId -> !existingUsers.contains(userId))
                .toList();
        if (CollectionUtils.isEmpty(userToAddList))
            return;
        List<SpaceRoleUserEntity> needSave = new ArrayList<>();
        for (UUID userId : userToAddList) {
            needSave.add(new SpaceRoleUserEntity()
                    .setSpaceRoleId(roleId)
                    .setUserId(userId)
                    .setTwinId(spaceId)
                    .setCreatedByUserId(currectApiUser.getUserId())
            );
        }
        // todo check user reg in BA and domain
//        List<UUID> usersOutOfList = userService.getUsersOutOfDomainAndBusinessAccount(needSave.stream().map(SpaceRoleUserEntity::getId).toList(), currectApiUser.getBusinessAccountId(), currectApiUser.getDomainId());
        if (CollectionUtils.isNotEmpty(needSave))
            entitySmartService.saveAllAndLog(needSave, spaceRoleUserRepository);
    }

    private void removeExtraUsers(UUID spaceId, UUID roleId, List<UUID> overrideList, List<UUID> existingUsers) {
        List<UUID> usersToRemove = existingUsers.stream()
                .filter(userId -> !overrideList.contains(userId))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(usersToRemove)) {
            spaceRoleUserRepository.deleteBySpaceIdAndSpaceRoleIdAndUserIdIn(spaceId, roleId, usersToRemove);
            usersToRemove.forEach(userId -> {
                log.info("user[" + userId + "] perhaps was deleted by space[" + spaceId + "] and role[" + roleId + "]");
            });
        }
    }
}
