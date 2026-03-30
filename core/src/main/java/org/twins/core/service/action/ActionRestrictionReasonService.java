package org.twins.core.service.action;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.action.ActionRestrictionReasonEntity;
import org.twins.core.dao.action.ActionRestrictionReasonRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ActionRestrictionReasonService extends EntitySecureFindServiceImpl<ActionRestrictionReasonEntity> {
    private final ActionRestrictionReasonRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<ActionRestrictionReasonEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<ActionRestrictionReasonEntity, UUID> entityGetIdFunction() {
        return ActionRestrictionReasonEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(ActionRestrictionReasonEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getDomainId() != null && !entity.getDomainId().equals(apiUser.getDomain().getId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(ActionRestrictionReasonEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        return true;
    }
}
