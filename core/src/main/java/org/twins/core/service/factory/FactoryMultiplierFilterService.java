package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
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

import java.util.Collection;
import java.util.Collections;
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

    public void loadFactoryMultiplierFilters(TwinFactoryMultiplierEntity multiplier) {
        loadFactoryMultiplierFilters(Collections.singletonList(multiplier));
    }

    public void loadFactoryMultiplierFilters(Collection<TwinFactoryMultiplierEntity> multipliers) {
        Kit<TwinFactoryMultiplierEntity, UUID> needLoad = new Kit<>(TwinFactoryMultiplierEntity::getId);
        for (TwinFactoryMultiplierEntity multiplier : multipliers) {
            if (multiplier.getTwinFactoryMultiplierFilterKit() == null)
                needLoad.add(multiplier);
        }
        if (needLoad.isEmpty())
            return;

        KitGrouped<TwinFactoryMultiplierFilterEntity, UUID, UUID> grouped = new KitGrouped<>(
            repository.findByTwinFactoryMultiplierIdIn(needLoad.getIdSet()),
            TwinFactoryMultiplierFilterEntity::getId,
            TwinFactoryMultiplierFilterEntity::getTwinFactoryMultiplierId);

        for (TwinFactoryMultiplierEntity multiplier : needLoad) {
            if (grouped.containsGroupedKey(multiplier.getId()))
                multiplier.setTwinFactoryMultiplierFilterKit(new Kit<>(grouped.getGrouped(multiplier.getId()), TwinFactoryMultiplierFilterEntity::getId));
            else
                multiplier.setTwinFactoryMultiplierFilterKit(Kit.emptyKit());
        }
    }
}
