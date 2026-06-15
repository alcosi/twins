package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.domain.factory.FactoryTriggerDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryTriggerDuplicateService extends EntityDuplicateService<FactoryTriggerDuplicate, TwinFactoryTriggerEntity> {

    @Lazy
    private final FactoryTriggerService factoryTriggerService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryTriggerEntity> entityService() {
        return factoryTriggerService;
    }

    @Override
    protected FactoryTriggerDuplicate createNewDuplicate() {
        return new FactoryTriggerDuplicate();
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void validateKeyUniqueness(Collection<FactoryTriggerDuplicate> duplicates) throws ServiceException {
        // triggers have no key concept
    }

    @Override
    protected TwinFactoryTriggerEntity createNewEntity(FactoryTriggerDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryTriggerEntity()
                .setTwinFactoryId(src.getTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setTwinTriggerId(src.getTwinTriggerId())
                .setAsync(src.getAsync())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryTriggerEntity src, TwinFactoryTriggerEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntityId(TwinFactoryTriggerEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinFactoryId(duplicateParentEntityId);
    }

    public void duplicateForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory) throws ServiceException {
        List<TwinFactoryTriggerEntity> triggers = fromFactory.getTwinFactoryTriggerKit().getList();
        if (triggers == null || triggers.isEmpty()) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryTriggerEntity>();
        for (TwinFactoryTriggerEntity originalTrigger : triggers) {
            TwinFactoryTriggerEntity duplicateTrigger = new TwinFactoryTriggerEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setInputTwinClassId(originalTrigger.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(originalTrigger.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(originalTrigger.getTwinFactoryConditionInvert())
                    .setTwinTriggerId(originalTrigger.getTwinTriggerId())
                    .setAsync(originalTrigger.getAsync())
                    .setDescription(originalTrigger.getDescription())
                    .setActive(originalTrigger.getActive());
            entitiesForSave.add(duplicateTrigger);
        }
        factoryTriggerService.saveSafe(entitiesForSave);
    }
}
