package org.twins.core.service.space;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserEntity;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.dto.rest.space.SpaceRoleUserRqDTOv1;
import org.twins.core.service.EntitySmartService;

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

    public List<UserEntity> findUserByRole(UUID twinId, UUID spaceRoleId) throws ServiceException {
        return spaceRoleUserRepository.findByTwinIdAndSpaceRoleId(twinId, spaceRoleId);
    }

    @Transactional
    public void manageForRoleUser(UUID spaceId, UUID roleId, UUID createUserId, SpaceRoleUserRqDTOv1 request) {
        addEntryRoleUserList(spaceId, roleId, createUserId, request.spaceRoleUserEnterList);
        deleteEntryRoleUserList(request.spaceRoleUserExitList);
    }

    private void addEntryRoleUserList(UUID spaceId, UUID roleId, UUID createUserId, List<UUID> spaceRoleUserEnterList) {
        if (!spaceRoleUserEnterList.isEmpty()) {
            List<SpaceRoleUserEntity> list = new ArrayList<>();
            for (UUID userId : spaceRoleUserEnterList) {
                if (!checkSeemEntityInDB(spaceId, roleId, userId)) {
                    SpaceRoleUserEntity entity = new SpaceRoleUserEntity();
                    entity.setId(UUID.randomUUID());
                    entity.setTwinId(spaceId);
                    entity.setSpaceRoleId(roleId);
                    entity.setUserId(userId);
                    entity.setCreatedByUserId(createUserId);
                    entity.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
                    list.add(entity);
                }
            }
            entitySmartService.saveAllAndLog(list, spaceRoleUserRepository);
        }
    }

    private void deleteEntryRoleUserList(List<UUID> spaceRoleUserExitList) {
        if (!spaceRoleUserExitList.isEmpty()) {
            for (UUID id : spaceRoleUserExitList) {
                spaceRoleUserRepository.deleteAllByUserId(id);
                log.info("SpaceRoleUser [" + id + "] was deleted");
            }
        }
    }

    private Boolean checkSeemEntityInDB(UUID spaceId, UUID roleId, UUID userId) {
        return spaceRoleUserRepository.existsByTwinIdAndRoleIdAndUserId(spaceId, roleId, userId);
    }

}
