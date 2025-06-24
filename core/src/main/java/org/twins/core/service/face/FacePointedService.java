package org.twins.core.service.face;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.face.PointedFace;

import java.util.UUID;

@Slf4j
@Service
@Lazy
public abstract class FacePointedService<T extends FacePointedEntity> extends FaceVariantsService<T> {
    @Autowired
    private FaceService faceService;

    @Autowired
    private FaceTwinPointerService faceTwinPointerService;

    public PointedFace<T> findPointedFace(UUID faceId, UUID currentTwinId) throws ServiceException {
        T singleVariant = findSingleVariant(faceId, currentTwinId);
        TwinEntity currentTwin = faceService.getRequestFacePointers().getCurrentTwin();
        TwinEntity targetTwin = null;
        if (singleVariant.getTargetTwinPointerId() == null) {
            targetTwin = currentTwin;
        } else {
            targetTwin = faceTwinPointerService.getPointer(singleVariant.getTargetTwinPointerId());
        }
        PointedFace<T> ret = new PointedFace<>();
        ret
                .setTargetTwinId(targetTwin.getId())
                .setConfig(singleVariant);
        return ret;
    }
}
