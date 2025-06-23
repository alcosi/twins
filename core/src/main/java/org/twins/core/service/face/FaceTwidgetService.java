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
public abstract class FaceTwidgetService<T extends FacePointedEntity> extends FaceVariantsService<T> {
    @Autowired
    private FaceService faceService;

    @Autowired
    private FacePointerService facePointerService;

    public PointedFace<T> findPointedFace(UUID faceId, UUID currentTwinId) throws ServiceException {
        T singleVariant = findSingleVariant(faceId, currentTwinId);
        TwinEntity currentTwin = faceService.getRequestFacePointers().getCurrentTwin();
        TwinEntity targetTwin = null;
        if (singleVariant.getTargetTwinFacePointerId() == null) {
            targetTwin = currentTwin;
        } else {
            targetTwin = facePointerService.getPointer(singleVariant.getTargetTwinFacePointerId());
        }
        PointedFace<T> ret = new PointedFace<>();
        ret
                .setTargetTwinId(targetTwin.getId())
                .setConfig(singleVariant);
        return ret;
    }
}
