package org.twins.core.service.twinclass;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerEntity;
import org.twins.core.dao.twinclass.TwinClassDynamicMarkerRepository;
import org.twins.core.dao.twinclass.TwinClassFreezeEntity;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassDynamicMarkerService extends EntitySecureFindServiceImpl<TwinClassDynamicMarkerEntity> {
    private final TwinClassDynamicMarkerRepository twinClassDynamicMarkerRepository;
    private final AuthService authService;

    @Override
    public CrudRepository<TwinClassDynamicMarkerEntity, UUID> entityRepository() {
        return twinClassDynamicMarkerRepository;
    }


    @Override
    public Function<TwinClassDynamicMarkerEntity, UUID> entityGetIdFunction() {
        return TwinClassDynamicMarkerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassDynamicMarkerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return entity.getTwinClass().getDomainId() != null && !entity.getTwinClass().getDomainId().equals(authService.getApiUser().getDomainId());
    }

    @Override
    public boolean validateEntity(TwinClassDynamicMarkerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public List<TwinClassDynamicMarkerEntity> findByTwinClassIdIn(Collection<UUID> twinClassIds) {
        return twinClassDynamicMarkerRepository.findByTwinClassIdIn(twinClassIds);
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassDynamicMarkerEntity> createTwinClassDynamicMarkerList(Collection<TwinClassDynamicMarkerEntity> dynamicMarkers) throws ServiceException {
        if (CollectionUtils.isEmpty(dynamicMarkers)) {
            return Collections.emptyList();
        }

        validateEntitiesAndThrow(dynamicMarkers, EntitySmartService.EntityValidateMode.beforeSave);

        return StreamSupport.stream(entityRepository().saveAll(dynamicMarkers).spliterator(), false).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinClassDynamicMarkerEntity> updateTwinClassDynamicMarkerList(Collection<TwinClassDynamicMarkerEntity> dynamicMarkers) throws ServiceException {
        if (CollectionUtils.isEmpty(dynamicMarkers)) {
            return Collections.emptyList();
        }

        Kit<TwinClassDynamicMarkerEntity, UUID> dbTwinClassDynamicMarkerEntityKit = findEntitiesSafe(
                dynamicMarkers.stream()
                        .map(TwinClassDynamicMarkerEntity::getId)
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinClassDynamicMarkerEntity> changes = new ChangesHelperMulti<>();

        for (TwinClassDynamicMarkerEntity twinClassDynamicMarkerEntity : dynamicMarkers) {
            ChangesHelper changesHelper = new ChangesHelper();
            TwinClassDynamicMarkerEntity dbTwinClassDynamicMarkerEntity = dbTwinClassDynamicMarkerEntityKit.get(twinClassDynamicMarkerEntity.getId());

            updateEntityFieldByEntity(twinClassDynamicMarkerEntity, dbTwinClassDynamicMarkerEntity,
                    TwinClassDynamicMarkerEntity::getTwinClassId, TwinClassDynamicMarkerEntity::setTwinClassId,
                    TwinClassDynamicMarkerEntity.Fields.twinClassId, changesHelper);
            updateEntityFieldByEntity(twinClassDynamicMarkerEntity, dbTwinClassDynamicMarkerEntity,
                    TwinClassDynamicMarkerEntity::getTwinValidatorSetId, TwinClassDynamicMarkerEntity::setTwinValidatorSetId,
                    TwinClassDynamicMarkerEntity.Fields.twinValidatorSetId, changesHelper);
            updateEntityFieldByEntity(twinClassDynamicMarkerEntity, dbTwinClassDynamicMarkerEntity,
                    TwinClassDynamicMarkerEntity::getMarkerDataListOptionId, TwinClassDynamicMarkerEntity::setMarkerDataListOptionId,
                    TwinClassDynamicMarkerEntity.Fields.markerDataListOptionId, changesHelper);

            changes.add(dbTwinClassDynamicMarkerEntity, changesHelper);
        }

        updateSafe(changes);

        return dbTwinClassDynamicMarkerEntityKit.getList();
    }
}
