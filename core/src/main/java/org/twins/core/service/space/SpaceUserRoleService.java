package org.twins.core.service.space;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleUserRepository;
import org.twins.core.dao.user.UserEntity;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceUserRoleService {
    final SpaceRoleUserRepository spaceRoleUserRepository;


    public List<UserEntity> findUserByRole(UUID twinId, UUID spaceRoleId) throws ServiceException {
        return spaceRoleUserRepository.findByTwinIdAndSpaceRoleId(twinId, spaceRoleId);
    }
}
