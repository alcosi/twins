package org.twins.core.service.space;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserRqDTOv1;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserRoleService {
    final EntitySmartService entitySmartService;
    final SpaceRoleUserRepository spaceRoleUserRepository;
    final AuthService authService;

    public List<UserEntity> findUserByRole(UUID twinId, UUID spaceRoleId) throws ServiceException {
        return spaceRoleUserRepository.findByTwinIdAndSpaceRoleId(twinId, spaceRoleId);
    }

    @Transactional
    public void manageForRoleUser(UUID spaceId, UUID roleId, SpaceRoleUserRqDTOv1 request) throws ServiceException {
        UUID createUserId = authService.getApiUser().getUser().getId();
        addEntryRoleUserList(spaceId, roleId, createUserId, request.spaceRoleUserEnterList);
        deleteEntryRoleUserList(spaceId, roleId, request.spaceRoleUserExitList);
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
