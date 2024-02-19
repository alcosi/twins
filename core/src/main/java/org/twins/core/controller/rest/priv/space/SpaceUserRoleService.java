package org.twins.core.controller.rest.priv.space;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.domain.space.SpaceRoleUserMap;
import org.twins.core.domain.space.SpaceRoleUserSearch;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserRoleService {
    final EntitySmartService entitySmartService;
    final SpaceRoleUserRepository spaceRoleUserRepository;
    final AuthService authService;

    // twinId is equivalent of spaceId

    public List<SpaceRoleUserMap> getAllUsersRefRolesBySpaceIdMap(UUID twinId) throws ServiceException {
        List<SpaceRoleUserEntity> spaceRoleUserEntities = spaceRoleUserRepository.findAllByTwinId(twinId);
        return createUserRoleMap(spaceRoleUserEntities);
    }


    public List<SpaceRoleUserMap> getUsersRefRolesMap(SpaceRoleUserSearch search, UUID spaceId) throws ServiceException {
        if(CollectionUtils.isEmpty(search.getRolesList()) && ObjectUtils.isEmpty(search.getNameLike())) return getAllUsersRefRolesBySpaceIdMap(spaceId);
        else {
            List<SpaceRoleUserEntity> spaceRoleUserEntities;
            String name = search.getNameLike();
            if (CollectionUtils.isEmpty(search.getRolesList()) && !ObjectUtils.isEmpty(name)) {
                spaceRoleUserEntities = spaceRoleUserRepository.findByTwinIdAndNameLike(spaceId, name);
            } else if(CollectionUtils.isNotEmpty(search.getRolesList()) && ObjectUtils.isEmpty(name)) {
                spaceRoleUserEntities = spaceRoleUserRepository.findByTwinIdAndRoleIn(spaceId, search.getRolesList());
            } else {
                spaceRoleUserEntities = spaceRoleUserRepository.findByTwinIdAndNameLikeAndRoleIn(spaceId, name, search.getRolesList());
            }
            return createUserRoleMap(spaceRoleUserEntities);
        }
    }

    private List<SpaceRoleUserMap> createUserRoleMap(List<SpaceRoleUserEntity> spaceRoleUserEntities) {
        List<SpaceRoleUserMap> result = new ArrayList<>();
        Map<UserEntity, List<SpaceRoleUserEntity>> map = new HashMap<>();
        for(SpaceRoleUserEntity item : spaceRoleUserEntities) {
            map.putIfAbsent(item.getUser(), new ArrayList<>());
            map.get(item.getUser()).add(item);
        }
        for(var entry : map.entrySet()) result.add(new SpaceRoleUserMap().setUser(entry.getKey()).addRoles(entry.getValue()));
        return result;
    }

    public List<UserEntity> findUserByRole(UUID twinId, UUID spaceRoleId) throws ServiceException {
        return spaceRoleUserRepository.findByTwinIdAndSpaceRoleId(twinId, spaceRoleId);
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
