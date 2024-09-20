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

    private List<SpaceRoleUserEntity> loadExistingUsers(UUID spaceId, UUID roleId) {
        return spaceRoleUserRepository.findAllByTwinIdAndSpaceRoleId(spaceId, roleId);
    }

    @Transactional
    public void manageSpaceRoleForUsers(UUID spaceId, UUID roleId, List<UUID> spaceRoleUserEnterList, List<UUID> spaceRoleUserExitList) throws ServiceException {
        addEntryRoleUserList(spaceId, roleId, spaceRoleUserEnterList);
        excludeUsersFromSpace(spaceId, roleId, spaceRoleUserExitList);
    }

    private void addEntryRoleUserList(UUID spaceId, UUID roleId, List<UUID> spaceRoleUserEnterList) throws ServiceException {
        if (CollectionUtils.isEmpty(spaceRoleUserEnterList))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<SpaceRoleUserEntity> listToAdd = new ArrayList<>();
        Set<UUID> existingUserSet = loadExistingUsers(spaceId, roleId).stream().map(SpaceRoleUserEntity::getUserId).collect(Collectors.toSet());
        for (UUID userId : spaceRoleUserEnterList) {
            if (existingUserSet.contains(userId)) {
                log.warn("user[" + userId + "] is already registered for role[" + roleId + "] in space[" + spaceId + "]");
                continue;
            }
            listToAdd.add(new SpaceRoleUserEntity()
                    .setTwinId(spaceId)
                    .setSpaceRoleId(roleId)
                    .setUserId(userId)
                    .setCreatedByUserId(apiUser.getUserId())
            );
        }
        if (CollectionUtils.isNotEmpty(listToAdd))
            entitySmartService.saveAllAndLog(listToAdd, spaceRoleUserRepository);
    }

    @Transactional
    public void overrideSpaceRoleUsers(UUID spaceId, UUID roleId, List<UUID> overrideList) throws ServiceException {
        if (CollectionUtils.isEmpty(overrideList))
            return;
        ApiUser apiUser = authService.getApiUser();
        Set<UUID> overrideSet = new HashSet<>(overrideList);
        List<UUID> deleteUserList = new ArrayList<>();
        List<SpaceRoleUserEntity> existingUsers = loadExistingUsers(spaceId, roleId);

        Iterator<SpaceRoleUserEntity> existingUsersIterator = existingUsers.iterator();
        while (existingUsersIterator.hasNext()) {
            SpaceRoleUserEntity existingUser = existingUsersIterator.next();
            UUID existingUserId = existingUser.getUserId();
            if (!overrideSet.contains(existingUserId))
                deleteUserList.add(existingUserId);
            else
                overrideSet.remove(existingUserId);
        }

        List<UUID> usersOutOfList = userService.getUsersOutOfDomainAndBusinessAccount(overrideSet, apiUser.getBusinessAccountId(), apiUser.getDomainId());
        if (CollectionUtils.isNotEmpty(usersOutOfList)) {
            for (UUID userId : usersOutOfList) {
                log.warn("user[{}] was not added because he is not registered in domain[{}] or business account[{}]", userId, apiUser.getDomainId(), apiUser.getBusinessAccountId());
            }
            usersOutOfList.forEach(overrideSet::remove);
        }

        includeUsersToSpace(spaceId, roleId, apiUser.getUserId(), overrideSet);
        excludeUsersFromSpace(spaceId, roleId, deleteUserList);
    }

    private void includeUsersToSpace(UUID spaceId, UUID roleId, UUID createUserId, Set<UUID> userList) {
        if (CollectionUtils.isEmpty(userList))
            return;
        List<SpaceRoleUserEntity> listToAdd = new ArrayList<>();
        for (UUID userId : userList) {
            listToAdd.add(new SpaceRoleUserEntity()
                    .setSpaceRoleId(roleId)
                    .setUserId(userId)
                    .setTwinId(spaceId)
                    .setCreatedByUserId(createUserId)
            );
        }
        if (CollectionUtils.isNotEmpty(listToAdd))
            entitySmartService.saveAllAndLog(listToAdd, spaceRoleUserRepository);
    }

    private void excludeUsersFromSpace(UUID spaceId, UUID roleId, List<UUID> deleteUserList) {
        if (CollectionUtils.isNotEmpty(deleteUserList)) {
            spaceRoleUserRepository.deleteBySpaceIdAndSpaceRoleIdAndUserIdIn(spaceId, roleId, deleteUserList);
            for (UUID userId : deleteUserList) {
                log.info("user[" + userId + "] perhaps was deleted by space[" + spaceId + "] and role[" + roleId + "]");
            }
        }
    }
}
