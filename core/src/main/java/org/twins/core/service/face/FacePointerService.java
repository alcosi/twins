package org.twins.core.service.face;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FacePointerEntity;
import org.twins.core.dao.face.FacePointerRepository;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.pointer.Pointer;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class FacePointerService extends EntitySecureFindServiceImpl<FacePointerEntity> {
    private final FacePointerRepository facePointerRepository;
    private final RequestFacePointers requestFacePointers;
    private final FeaturerService featurerService;
    private final FaceService faceService;

    @Override
    public CrudRepository<FacePointerEntity, UUID> entityRepository() {
        return facePointerRepository;
    }

    @Override
    public Function<FacePointerEntity, UUID> entityGetIdFunction() {
        return FacePointerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(FacePointerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return entity.getFaceId() != null && !faceService.isEntityReadDenied(entity.getFace());
    }

    @Override
    public boolean validateEntity(FacePointerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty id");
        if (entity.getPointerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty pointerFeaturerId");
        return true;
    }

    public TwinEntity getPointer(UUID faceTwinPointerId) throws ServiceException {
        if (requestFacePointers.hasPointer(faceTwinPointerId)) {
            return requestFacePointers.getPointedTwin(faceTwinPointerId);
        }
        FacePointerEntity faceTwinPointer = findEntitySafe(faceTwinPointerId);
        Pointer pointer = featurerService.getFeaturer(faceTwinPointer.getPointerFeaturerId(), Pointer.class);
        TwinEntity targetTwin = pointer.point(faceTwinPointer.getPointerParams(), requestFacePointers.getCurrentTwin());
        requestFacePointers.addPointer(faceTwinPointerId, targetTwin);
        return targetTwin;
    }
}
