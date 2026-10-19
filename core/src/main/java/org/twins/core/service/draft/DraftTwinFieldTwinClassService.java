package org.twins.core.service.draft;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.draft.DraftTwinFieldTwinClassEntity;
import org.twins.core.dao.draft.DraftTwinFieldTwinClassRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class DraftTwinFieldTwinClassService extends TwinsEntitySecureFindService<DraftTwinFieldTwinClassEntity> {
    @Getter
    private final DraftTwinFieldTwinClassRepository repository;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<DraftTwinFieldTwinClassEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<DraftTwinFieldTwinClassEntity, UUID> entityGetIdFunction() {
        return DraftTwinFieldTwinClassEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(DraftTwinFieldTwinClassEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(DraftTwinFieldTwinClassEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public void loadTwinClass(DraftTwinFieldTwinClassEntity src) throws ServiceException {
        loadTwinClass(Collections.singletonList(src));
    }

    public void loadTwinClass(Collection<DraftTwinFieldTwinClassEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                DraftTwinFieldTwinClassEntity::getTwinClassId,
                DraftTwinFieldTwinClassEntity::getTwinClassEntity,
                DraftTwinFieldTwinClassEntity::setTwinClassEntity);
    }
}
