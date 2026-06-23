package org.twins.core.service.space;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.common.util.StringUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.history.context.HistoryContextSpaceRoleUserChange;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.enums.history.HistoryType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.history.HistoryItem;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.user.UserService;

import java.util.*;
import java.util.function.Function;

import static org.twins.core.dao.specifications.CommonSpecification.checkFieldUuid;
import static org.twins.core.dao.specifications.CommonSpecification.checkUuidIn;
import static org.twins.core.dao.specifications.space.SpaceRoleUserSpecification.*;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class SpaceRoleUserService extends TwinsEntitySecureFindService<SpaceRoleUserEntity> {
    @Getter
    private final SpaceRoleUserRepository repository;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final SpaceRoleService spaceRoleService;
    @Lazy
    private final UserService userService;
    private final AuthService authService;
    private final HistoryService historyService;

    @Override
    public CrudRepository<SpaceRoleUserEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<SpaceRoleUserEntity, UUID> entityGetIdFunction() {
        return SpaceRoleUserEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SpaceRoleUserEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(SpaceRoleUserEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public PaginationResult<UserRefSpaceRole> getAllUsersRefRolesBySpaceIdMap(UUID twinId, SimplePagination pagination) throws ServiceException {
        return getAllUsersRefRolesBySpaceIdMap(twinService.findEntitySafe(twinId), pagination);
    }

    public PaginationResult<UserRefSpaceRole> getAllUsersRefRolesBySpaceIdMap(TwinEntity twinEntity, SimplePagination pagination) throws ServiceException {
        Page<SpaceRoleUserEntity> spaceRoleUserEntities = repository.findAll(
                checkUuid(SpaceRoleUserEntity.Fields.twinId, twinEntity.getId(), false), PaginationUtils.pageableOffset(pagination)
        );
        return createUserRoleMap(spaceRoleUserEntities, pagination);
    }

    public UserRefSpaceRole getUsersRefRolesMapById(UUID spaceId,UUID userId) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(spaceId);

        List<SpaceRoleUserEntity> list = repository.findAll(
                Specification.allOf(
                        checkFieldUuid( twinEntity.getId(), SpaceRoleUserEntity.Fields.twinId),
                        checkFieldUuid(authService.getApiUser().getDomainId(), SpaceRoleUserEntity.Fields.twinSpecOnly, TwinEntity.Fields.twinClass, TwinClassEntity.Fields.domainId),
                        checkFieldUuid(userId, SpaceRoleUserEntity.Fields.id)
                )
        );
        UserRefSpaceRole userRefSpaceRole = new UserRefSpaceRole();
        userRefSpaceRole.setRoles(list);
        userRefSpaceRole.setUser(userService.findEntitySafe(userId));
        return userRefSpaceRole;
    }

    public PaginationResult<UserRefSpaceRole> getUsersRefRolesMap(SpaceRoleUserSearch search, UUID twinId, SimplePagination pagination) throws ServiceException {
        TwinEntity twinEntity = twinService.findEntitySafe(twinId);
        Specification<SpaceRoleUserEntity> spec =
                checkUuid(SpaceRoleUserEntity.Fields.twinId, twinEntity.getId(), false)
                        .and(checkUserNameLikeWithPattern(search.getUserNameLike()))
                        .and(checkUuidIn(search.getSpaceRolesIdList(), false, false, SpaceRoleUserEntity.Fields.spaceRoleId))
                        .and(checkUserInGroups(search.getUserGroupIdList(), false));
        Page<SpaceRoleUserEntity> spaceRoleUserEntities = repository.findAll(spec, PaginationUtils.pageableOffset(pagination));
        return createUserRoleMap(spaceRoleUserEntities, pagination);
    }

    private PaginationResult<UserRefSpaceRole> createUserRoleMap(Page<SpaceRoleUserEntity> spaceRoleUserEntities, SimplePagination pagination) throws ServiceException {
        List<UserRefSpaceRole> resultList = new ArrayList<>();
        Map<UserEntity, List<SpaceRoleUserEntity>> map = new HashMap<>();
        loadUser(spaceRoleUserEntities.stream().toList());
        for (SpaceRoleUserEntity item : spaceRoleUserEntities) {
            map.putIfAbsent(item.getUser(), new ArrayList<>());
            map.get(item.getUser()).add(item);
        }
        for (var entry : map.entrySet())
            resultList.add(new UserRefSpaceRole().setUser(entry.getKey()).addRoles(entry.getValue()));
        return PaginationUtils.convertInPaginationResult(resultList, pagination, spaceRoleUserEntities.getTotalElements());
    }

    public List<UserEntity> findUserBySpaceIdAndRoleId(UUID spaceId, UUID roleId) throws ServiceException {
        return repository.findByTwinIdAndSpaceRoleId(spaceId, roleId);
    }

    public List<SpaceRoleUserEntity> findSpaceRoleUsersByTwinIdAndUserId(UUID twinId, UUID userId) {
        return repository.findAllByTwinIdAndUserId(twinId, userId);
    }

    public List<SpaceRoleUserEntity> findAllByTwinIdAndRoleIdAndUserIds(UUID twinId, UUID roleId, Collection<UUID> userIds) {
        return repository.findAllByTwinIdAndSpaceRoleIdAndUserIdIn(twinId, roleId, userIds);
    }

    private Kit<SpaceRoleUserEntity, UUID> getExistingUsers(UUID spaceId, UUID roleId) {
        return new Kit<>(repository.findAllByTwinIdAndSpaceRoleId(spaceId, roleId), SpaceRoleUserEntity::getUserId);
    }

    @Transactional
    public void manageSpaceRoleForUsers(UUID spaceId, UUID roleId, List<UUID> spaceRoleUserEnterList, List<UUID> spaceRoleUserExitList) throws ServiceException {
        Set<UUID> usersToAdd = new HashSet<>();
        Set<UUID> usersToDelete = new HashSet<>();
        Kit<SpaceRoleUserEntity, UUID> existingUserKit = getExistingUsers(spaceId, roleId);
        if (CollectionUtils.isNotEmpty(spaceRoleUserEnterList)) {
            for (UUID userId : spaceRoleUserEnterList) {
                if (existingUserKit.containsKey(userId)) {
                    log.warn("user[{}] is already registered for role[{}] in space[{}]", userId, roleId, spaceId);
                    continue;
                }
                usersToAdd.add(userId);
            }
        }
        if (CollectionUtils.isNotEmpty(spaceRoleUserExitList)) {
            for (UUID userId : spaceRoleUserExitList) {
                if (!existingUserKit.containsKey(userId)) {
                    log.warn("user[{}] is not registered for role[{}] in space[{}]", userId, roleId, spaceId);
                    continue;
                }
                usersToDelete.add(userId);
            }
        }
        addUsersToSpaceRole(spaceId, roleId, usersToAdd);
        deleteUsersFromSpaceRole(spaceId, roleId, usersToDelete);
    }

    @Transactional
    public void overrideSpaceRoleUsers(UUID spaceId, UUID roleId, List<UUID> overrideList) throws ServiceException {
        SpaceRoleUserChanges spaceRoleUserChanges = calculateSpaceRoleUserChanges(spaceId, roleId, overrideList);
        applySpaceRoleUserChanges(spaceId, roleId, spaceRoleUserChanges);
    }

    public SpaceRoleUserChanges calculateSpaceRoleUserChanges(UUID spaceId, UUID roleId, List<UUID> overrideList) {
        // if overrideList is null or empty we need to remove all users from space role
        Set<UUID> overrideSet = overrideList != null ? new HashSet<>(overrideList) : new HashSet<>();
        Set<UUID> usersToDelete = new HashSet<>();
        Kit<SpaceRoleUserEntity, UUID> existingUserKit = getExistingUsers(spaceId, roleId);

        for (UUID existingUserId : existingUserKit.getIdSet()) {
            if (overrideSet.contains(existingUserId)) {
                overrideSet.remove(existingUserId); // this user is already in space, so it should not be added one more time
            } else {
                usersToDelete.add(existingUserId);
            }
        }
        return new SpaceRoleUserChanges(overrideSet, usersToDelete);
    }

    public void applySpaceRoleUserChanges(UUID spaceId, UUID roleId, SpaceRoleUserChanges spaceRoleUserChanges) throws ServiceException {
        addUsersToSpaceRole(spaceId, roleId, spaceRoleUserChanges.getAddUsers());
        deleteUsersFromSpaceRole(spaceId, roleId, spaceRoleUserChanges.getDeleteUsers());
    }

    private void addUsersToSpaceRole(UUID spaceId, UUID roleId, Set<UUID> userList) throws ServiceException {
        if (CollectionUtils.isEmpty(userList))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<UUID> invalidUsers = userService.getUsersOutOfDomainAndBusinessAccount(userList, apiUser.getBusinessAccountId(), apiUser.getDomainId());
        if (CollectionUtils.isNotEmpty(invalidUsers)) {
            throw new ServiceException(ErrorCodeTwins.USER_UNKNOWN, "Users[" + StringUtils.join(invalidUsers, ",") +"] can not be added because they are out of current BA or Domain");
        }
        List<SpaceRoleUserEntity> listToAdd = new ArrayList<>();
        for (UUID userId : userList) {
            listToAdd.add(new SpaceRoleUserEntity()
                    .setSpaceRoleId(roleId)
                    .setUserId(userId)
                    .setTwinId(spaceId)
                    .setCreatedByUserId(authService.getApiUser().getUserId())
            );
        }
        if (CollectionUtils.isNotEmpty(listToAdd)) {
            entitySmartService.saveAllAndLog(listToAdd, repository);
            List<UUID> userIdList = new ArrayList<>();
            for (SpaceRoleUserEntity entity : listToAdd) {
                userIdList.add(entity.getUserId());
            }
            TwinEntity twinEntity = twinService.findEntitySafe(spaceId);
            HistoryItem<HistoryContextSpaceRoleUserChange> historyItem = historyService.spaceRoleUserAdd(twinEntity, roleId, userIdList);
            historyService.saveHistory(twinEntity, historyItem.getType(), historyItem.getContext());
        }
    }

    public void deleteUsersFromSpaceRole(UUID spaceId, UUID roleId, Set<UUID> deleteUserList) throws ServiceException {
        if (CollectionUtils.isEmpty(deleteUserList))
            return;
        repository.deleteBySpaceIdAndSpaceRoleIdAndUserIdIn(spaceId, roleId, deleteUserList);
        List<UUID> userIdList = new ArrayList<>();
        for (UUID userId : deleteUserList) {
            log.info("user[{}] perhaps was deleted by space[{}}] and role[{}}]", userId, spaceId, roleId);
            userIdList.add(userId);
        }
        HistoryContextSpaceRoleUserChange context = new HistoryContextSpaceRoleUserChange()
                .setRoleId(roleId)
                .setTargetedUserIds(userIdList);
        historyService.saveHistory(twinService.findEntitySafe(spaceId), HistoryType.spaceRoleUserRemoved, context);
    }

    @Getter
    @AllArgsConstructor
    public static class SpaceRoleUserChanges {
        Set<UUID> addUsers;
        Set<UUID> deleteUsers;
    }

    public Set<UUID> getUsers(UUID twinId, Set<UUID> spaceRoleIds) {
        return repository.findUserIdsByTwinIdAndSpaceRoleIds(twinId, spaceRoleIds);
    }

    public void loadTwin(SpaceRoleUserEntity src) throws ServiceException {
        loadTwin(Collections.singletonList(src));
    }

    public void loadTwin(Collection<SpaceRoleUserEntity> srcCollection) throws ServiceException {
        twinService.load(srcCollection,
                SpaceRoleUserEntity::getTwinId,
                SpaceRoleUserEntity::getTwin,
                SpaceRoleUserEntity::setTwin);
    }

    public void loadSpaceRole(SpaceRoleUserEntity src) throws ServiceException {
        loadSpaceRole(Collections.singletonList(src));
    }

    public void loadSpaceRole(Collection<SpaceRoleUserEntity> srcCollection) throws ServiceException {
        spaceRoleService.load(srcCollection,
                SpaceRoleUserEntity::getSpaceRoleId,
                SpaceRoleUserEntity::getSpaceRole,
                SpaceRoleUserEntity::setSpaceRole);
    }

    public void loadUser(SpaceRoleUserEntity src) throws ServiceException {
        loadUser(Collections.singletonList(src));
    }

    public void loadUser(Collection<SpaceRoleUserEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                SpaceRoleUserEntity::getUserId,
                SpaceRoleUserEntity::getUser,
                SpaceRoleUserEntity::setUser);
    }
}
