package org.twins.face.service.twidget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.face.TwidgetConfig;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.face.FaceTwidgetService;
import org.twins.face.dao.twiget.FaceTW001Entity;
import org.twins.face.dao.twiget.FaceTW001Repository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTW001Service extends FaceTwidgetService<FaceTW001Entity> {
    private final FaceTW001Repository faceTW001Repository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<FaceTW001Entity, UUID> entityRepository() {
        return faceTW001Repository;
    }

    @Override
    public Function<FaceTW001Entity, UUID> entityGetIdFunction() {
        return FaceTW001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTW001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getFace().getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceTW001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public TwidgetConfig<FaceTW001Entity> getConfig(UUID faceId, UUID currentTwinId) throws ServiceException {
        TwidgetConfig<FaceTW001Entity> ret = new TwidgetConfig<>();
        ret
                .setTargetTwinId(currentTwinId) //should be replaced in future
                .setConfig(findEntitySafe(faceId));
        return ret;
    }
}
