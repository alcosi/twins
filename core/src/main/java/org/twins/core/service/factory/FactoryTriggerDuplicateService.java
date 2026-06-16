package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryTriggerEntity;
import org.twins.core.domain.EntityDuplicateContext;
import org.twins.core.domain.factory.FactoryTriggerDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryTriggerDuplicateService extends EntityDuplicateService<FactoryTriggerDuplicate, TwinFactoryTriggerEntity, TwinFactoryEntity> {

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
    protected void loadFor(Collection<TwinFactoryEntity> parents) {
        factoryTriggerService.loadFactoryTriggers(parents);
    }

    @Override
    protected Kit<TwinFactoryTriggerEntity, UUID> extractorChildren(TwinFactoryEntity parent) {
        return parent.getTwinFactoryTriggerKit();
    }

    @Override
    protected UUID extractParentId(TwinFactoryEntity parent) {
        return parent.getId();
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

    @Override
    protected void remapReferences(TwinFactoryTriggerEntity newEntity, EntityDuplicateContext ctx) {
        newEntity.setTwinFactoryConditionSetId(
                ctx.resolveOrDefault(TwinFactoryConditionSetEntity.class, newEntity.getTwinFactoryConditionSetId()));
    }
}
