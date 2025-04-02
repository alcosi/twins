package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.service.face.FaceService;
import org.twins.face.dao.widget.wt001.FaceWT001Entity;
import org.twins.face.dao.widget.wt001.FaceWT001Repository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT001Service extends EntitySecureFindServiceImpl<FaceWT001Entity> {
    private final FaceWT001Repository faceWT001Repository;
    private final FaceService faceService;

    @Override
    public CrudRepository<FaceWT001Entity, UUID> entityRepository() {
        return faceWT001Repository;
    }

    @Override
    public Function<FaceWT001Entity, UUID> entityGetIdFunction() {
        return FaceWT001Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT001Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FaceWT001Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
