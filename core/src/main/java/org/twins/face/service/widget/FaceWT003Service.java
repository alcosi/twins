package org.twins.face.service.widget;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.face.dao.widget.wt003.FaceWT003Entity;
import org.twins.face.dao.widget.wt003.FaceWT003Repository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceWT003Service extends EntitySecureFindServiceImpl<FaceWT003Entity> {
    private final FaceWT003Repository faceWT003Repository;

    @Override
    public CrudRepository<FaceWT003Entity, UUID> entityRepository() {
        return faceWT003Repository;
    }

    @Override
    public Function<FaceWT003Entity, UUID> entityGetIdFunction() {
        return FaceWT003Entity::getFaceId;
    }

    @Override
    public boolean isEntityReadDenied(FaceWT003Entity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(FaceWT003Entity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
