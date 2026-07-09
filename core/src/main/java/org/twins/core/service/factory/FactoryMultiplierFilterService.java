package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterRepository;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryMultiplierFilterService extends EntitySecureFindServiceImpl<TwinFactoryMultiplierFilterEntity> {
    @Getter
    private final TwinFactoryMultiplierFilterRepository repository;
    private final AuthService authService;
    @Lazy
    private final FactoryConditionSetService factoryConditionSetService;
    @Lazy
    private final FactoryMultiplierService factoryMultiplierService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinFactoryMultiplierFilterEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryMultiplierFilterEntity, UUID> entityGetIdFunction() {
        return TwinFactoryMultiplierFilterEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryMultiplierFilterEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=false;
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryMultiplierFilterEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity,EntitySmartService.ReadPermissionCheckMode.none);
    }

    public List<TwinFactoryMultiplierFilterEntity> findByTwinFactoryMultiplierIdIn(Collection<UUID> multiplierIds) {
        return repository.findByTwinFactoryMultiplierIdIn(multiplierIds);
    }

    public void loadFactoryMultiplierFilters(TwinFactoryMultiplierEntity multiplier) {
        if (multiplier.getTwinFactoryMultiplierFilterKit() != null)
            return;
        loadFactoryMultiplierFilters(Collections.singletonList(multiplier));
    }

    public void loadFactoryMultiplierFilters(Collection<TwinFactoryMultiplierEntity> multipliers) {
        loadKit(
                multipliers,
                TwinFactoryMultiplierEntity::getId,
                TwinFactoryMultiplierEntity::getTwinFactoryMultiplierFilterKit,
                TwinFactoryMultiplierEntity::setTwinFactoryMultiplierFilterKit,
                repository::findByTwinFactoryMultiplierIdIn,
                TwinFactoryMultiplierFilterEntity::getId,
                TwinFactoryMultiplierFilterEntity::getTwinFactoryMultiplierId);
    }

    public void loadConditionSet(TwinFactoryMultiplierFilterEntity filter) throws ServiceException {
        loadConditionSet(Collections.singleton(filter));
    }

    public void loadConditionSet(Collection<TwinFactoryMultiplierFilterEntity> filters) throws ServiceException {
        factoryConditionSetService.load(filters,
                TwinFactoryMultiplierFilterEntity::getTwinFactoryConditionSetId,
                TwinFactoryMultiplierFilterEntity::getConditionSet,
                TwinFactoryMultiplierFilterEntity::setConditionSet);
    }

    public void loadMultiplier(TwinFactoryMultiplierFilterEntity src) throws ServiceException {
        loadMultiplier(Collections.singletonList(src));
    }

    public void loadMultiplier(List<TwinFactoryMultiplierFilterEntity> srcCollection) throws ServiceException {
        factoryMultiplierService.load(srcCollection,
                TwinFactoryMultiplierFilterEntity::getTwinFactoryMultiplierId,
                TwinFactoryMultiplierFilterEntity::getMultiplier,
                TwinFactoryMultiplierFilterEntity::setMultiplier);
    }

    public void loadInputTwinClass(TwinFactoryMultiplierFilterEntity filter) throws ServiceException {
        loadInputTwinClass(Collections.singleton(filter));
    }

    public void loadInputTwinClass(Collection<TwinFactoryMultiplierFilterEntity> filters) throws ServiceException {
        twinClassService.load(filters,
                TwinFactoryMultiplierFilterEntity::getInputTwinClassId,
                TwinFactoryMultiplierFilterEntity::getInputTwinClass,
                TwinFactoryMultiplierFilterEntity::setInputTwinClass);
    }
}
