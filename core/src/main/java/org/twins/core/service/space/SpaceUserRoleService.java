package org.twins.core.service.space;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.common.util.PaginationUtils;
import org.cambium.service.EntitySmartService;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.domain.space.UserRefSpaceRole;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

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
