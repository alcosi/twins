package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.search.TwinSearchEntity;
import org.twins.core.dao.search.TwinSearchRepository;
import org.twins.core.service.TwinsEntitySecureFindService;

import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class TwinSearchServiceV3 extends TwinsEntitySecureFindService<TwinSearchEntity> {
    private final TwinSearchRepository twinSearchRepository;

    @Override
    public CrudRepository<TwinSearchEntity, UUID> entityRepository() {
        return twinSearchRepository;
    }

    @Override
    public Function<TwinSearchEntity, UUID> entityGetIdFunction() {
        return TwinSearchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinSearchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinSearchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
