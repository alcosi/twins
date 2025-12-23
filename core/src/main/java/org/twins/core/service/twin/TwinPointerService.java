package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinPointerEntity;
import org.twins.core.dao.twin.TwinPointerRepository;
import org.twins.core.featurer.pointer.Pointer;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinPointerService extends EntitySecureFindServiceImpl<TwinPointerEntity> {
    private final TwinPointerRepository twinPointerRepository;
    private final FeaturerService featurerService;
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinPointerEntity, UUID> entityRepository() {
        return twinPointerRepository;
    }

    @Override
    public Function<TwinPointerEntity, UUID> entityGetIdFunction() {
        return TwinPointerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinPointerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return entity.getTwinClassId() != null && twinClassService.isEntityReadDenied(entity.getTwinClass());
    }

    @Override
    public boolean validateEntity(TwinPointerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty id");
        if (entity.getPointerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty pointerFeaturerId");
        return true;
    }

    public TwinEntity getPointer(TwinEntity currentTwin, UUID twinPointerId) throws ServiceException {
        TwinPointerEntity twinPointer = findEntitySafe(twinPointerId);
        Pointer pointer = featurerService.getFeaturer(twinPointer.getPointerFeaturerId(), Pointer.class);
        return pointer.point(twinPointer.getPointerParams(), currentTwin);
    }
}
