package org.twins.core.service.face;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.face.FacePointedEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.face.PointedFace;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
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
            log.info("target twin pointer[{}] is configured for face[{}]", singleVariant.getTargetTwinPointerId(), faceId);
            targetTwin = faceTwinPointerService.getPointer(singleVariant.getTargetTwinPointerId());
        }
        if (targetTwin == null) {
            throw new ServiceException(ErrorCodeTwins.POINTER_ON_NULL, "can not detect target twin");
        }
        PointedFace<T> ret = new PointedFace<>();
        ret
                .setTargetTwinId(targetTwin.getId())
                .setConfig(singleVariant);
        return ret;
    }
}
