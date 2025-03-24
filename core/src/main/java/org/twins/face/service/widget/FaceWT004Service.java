package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.face.dao.widget.FaceWT004Entity;
import org.twins.face.dao.widget.FaceWT004Repository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT004Service extends EntitySecureFindServiceImpl<FaceWT004Entity> {
    private final FaceWT004Repository faceWT004Repository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<FaceWT004Entity, UUID> entityRepository() {
        return faceWT004Repository;
    }

    @Override
    public Function<FaceWT004Entity, UUID> entityGetIdFunction() {
        return FaceWT004Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT004Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getFace().getDomainId().equals(authService.getApiUser().getDomainId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allows in domain[" + apiUser.getDomainId() + "]");
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT004Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
