package org.twins.core.service.validator;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorRepository;
import org.twins.core.service.twin.TwinValidatorSetService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;


@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinValidatorService extends EntitySecureFindServiceImpl<TwinValidatorEntity> {
    private final TwinValidatorRepository twinValidatorRepository;
    @Lazy
    private final TwinValidatorSetService twinValidatorSetService;

    @Override
    public CrudRepository<TwinValidatorEntity, UUID> entityRepository() {
        return twinValidatorRepository;
    }

    @Override
    public Function<TwinValidatorEntity, UUID> entityGetIdFunction() {
        return TwinValidatorEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinValidatorEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinValidatorEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(T entity) throws ServiceException {
        loadValidators(List.of(entity));
    }

    public <T extends ContainsTwinValidatorSet> void loadValidators(Collection<T> entities) throws ServiceException {
        loadKit(entities,
                ContainsTwinValidatorSet::getTwinValidatorSetId,
                ContainsTwinValidatorSet::getTwinValidatorKit,
                ContainsTwinValidatorSet::setTwinValidatorKit,
                twinValidatorRepository::findByTwinValidatorSetIdIn,
                TwinValidatorEntity::getId,
                TwinValidatorEntity::getTwinValidatorSetId,
                (child, parent) -> {});
    }

    public void loadTwinValidatorSet(TwinValidatorEntity src) throws ServiceException {
        twinValidatorSetService.loadTwinValidatorSet(src);
    }

    public void loadTwinValidatorSet(Collection<TwinValidatorEntity> srcCollection) throws ServiceException {
        twinValidatorSetService.loadTwinValidatorSet(srcCollection);
    }
}