package org.twins.core.service.face;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FaceTwinPointerEntity;
import org.twins.core.dao.face.FaceTwinPointerRepository;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FaceTwinPointerService extends EntitySecureFindServiceImpl<FaceTwinPointerEntity> {
    private final FaceTwinPointerRepository faceTwinPointerRepository;

    @Override
    public CrudRepository<FaceTwinPointerEntity, UUID> entityRepository() {
        return faceTwinPointerRepository;
    }

    @Override
    public Function<FaceTwinPointerEntity, UUID> entityGetIdFunction() {
        return FaceTwinPointerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FaceTwinPointerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) {
        return false;
    }

    @Override
    public boolean validateEntity(FaceTwinPointerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty id");
        if (entity.getFaceId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty faceId");
        if (entity.getPointerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty pointerFeaturerId");
        return true;
    }
}
