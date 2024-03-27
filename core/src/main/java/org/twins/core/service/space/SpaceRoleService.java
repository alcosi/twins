package org.twins.core.service.space;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.space.SpaceRoleRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.EntitySmartService;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpaceRoleService {

    @Lazy
    final EntitySmartService entitySmartService;

    @Lazy
    final AuthService authService;

    final SpaceRoleRepository spaceRoleRepository;

    public void forceDeleteRoles(UUID businessAccountId) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        UUID domainId = apiUser.getDomainId();

        spaceRoleRepository.deleteAllByBusinessAccountIdAndDomainId(businessAccountId, domainId);
    }
}
