package org.twins.core.service.face;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceEntity;
import org.twins.core.dao.face.FaceRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceService extends EntitySecureFindServiceImpl<FaceEntity> {
    private final FaceRepository faceRepository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<FaceEntity, UUID> entityRepository() {
        return faceRepository;
    }

    @Override
    public Function<FaceEntity, UUID> entityGetIdFunction() {
        return FaceEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
