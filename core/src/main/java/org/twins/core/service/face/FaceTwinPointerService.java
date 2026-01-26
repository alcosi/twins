package org.twins.core.service.face;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.twin.TwinPointerService;

import java.util.UUID;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Lazy
@RequiredArgsConstructor
public class FaceTwinPointerService  {
    private final TwinPointerService twinPointerService;
    private final RequestTwinPointers requestFacePointers;

    public TwinEntity getPointer(UUID faceTwinPointerId) throws ServiceException {
        if (requestFacePointers.hasPointer(faceTwinPointerId)) {
            return requestFacePointers.getPointedTwin(faceTwinPointerId);
        }
        TwinEntity targetTwin = twinPointerService.getPointer(requestFacePointers.getCurrentTwin(), faceTwinPointerId);
        requestFacePointers.addPointer(faceTwinPointerId, targetTwin);
        return targetTwin;
    }
}
