package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetRepository;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@Lazy
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@AllArgsConstructor
public class FactoryConditionSetService extends EntitySecureFindServiceImpl<TwinFactoryConditionSetEntity> {
    @Getter
    private final TwinFactoryConditionSetRepository repository;
    private final AuthService authService;
    private final FactoryService factoryService;

    @Override
    public CrudRepository<TwinFactoryConditionSetEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinFactoryConditionSetEntity, UUID> entityGetIdFunction() {
        return TwinFactoryConditionSetEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryConditionSetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryConditionSetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinFactoryConditionSetEntity> createFactoryConditionSet(
            List<TwinFactoryConditionSetEntity> conditionSetCreates
    ) throws ServiceException {
        if (CollectionUtils.isEmpty(conditionSetCreates)) {
            return Collections.emptyList();
        }
        UUID apiUserId = authService.getApiUser().getUserId();
        UUID domainId = authService.getApiUser().getDomainId();
        for (TwinFactoryConditionSetEntity twinFactoryConditionSetEntity : conditionSetCreates) {
            twinFactoryConditionSetEntity
                    .setCreatedAt(Timestamp.valueOf(LocalDateTime.now()))
                    .setCreatedByUserId(apiUserId)
                    .setDomainId(domainId);
        validateEntityAndThrow(twinFactoryConditionSetEntity, EntitySmartService.EntityValidateMode.beforeSave);
        }
        return StreamSupport.stream(
                entityRepository().saveAll(conditionSetCreates).spliterator(), false
        ).toList();
    }

    @Transactional(rollbackFor = Throwable.class)
    public List<TwinFactoryConditionSetEntity> updateFactoryConditionSet(
            List<TwinFactoryConditionSetEntity> conditionSetUpdates
    ) throws ServiceException {
        if (CollectionUtils.isEmpty(conditionSetUpdates)) {
            return Collections.emptyList();
        }

        Kit<TwinFactoryConditionSetEntity, UUID> dbFactoryConditionSetKit = findEntitiesSafe(
                conditionSetUpdates.stream()
                        .map(TwinFactoryConditionSetEntity::getId)
                        .collect(Collectors.toList())
        );

        ChangesHelperMulti<TwinFactoryConditionSetEntity> changes = new ChangesHelperMulti<>();

        for (TwinFactoryConditionSetEntity twinFactoryConditionSetEntity : conditionSetUpdates) {
            ChangesHelper changesHelper = new ChangesHelper();
            TwinFactoryConditionSetEntity dbFactoryConditionSetEntity = dbFactoryConditionSetKit.get(
                    twinFactoryConditionSetEntity.getId()
            );
            updateEntityFieldByEntity(twinFactoryConditionSetEntity, dbFactoryConditionSetEntity,
                    TwinFactoryConditionSetEntity::getName, TwinFactoryConditionSetEntity::setName,
                    TwinFactoryConditionSetEntity.Fields.name, changesHelper);
            updateEntityFieldByEntity(twinFactoryConditionSetEntity, dbFactoryConditionSetEntity,
                    TwinFactoryConditionSetEntity::getDescription, TwinFactoryConditionSetEntity::setDescription,
                    TwinFactoryConditionSetEntity.Fields.description, changesHelper);
            updateEntityFieldByEntity(twinFactoryConditionSetEntity, dbFactoryConditionSetEntity,
                    TwinFactoryConditionSetEntity::getTwinFactoryId, TwinFactoryConditionSetEntity::setTwinFactoryId,
                    TwinFactoryConditionSetEntity.Fields.twinFactoryId, changesHelper);

            dbFactoryConditionSetEntity.setUpdatedAt(Timestamp.from(Instant.now()));
            changes.add(dbFactoryConditionSetEntity, changesHelper);
        }
        updateSafe(changes);
        return dbFactoryConditionSetKit.getList();
    }

    public void loadFactory(TwinFactoryConditionSetEntity conditionSet) throws ServiceException {
        loadFactory(Collections.singletonList(conditionSet));
    }

    public void loadFactory(Collection<TwinFactoryConditionSetEntity> collection) throws ServiceException {
        factoryService.load(collection,
                TwinFactoryConditionSetEntity::getId,
                TwinFactoryConditionSetEntity::getTwinFactoryId,
                TwinFactoryConditionSetEntity::getTwinFactory,
                TwinFactoryConditionSetEntity::setTwinFactory);
    }
}
