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
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dao.factory.TwinFactoryMultiplierFilterEntity;
import org.twins.core.domain.EntityDuplicateContext;
import org.twins.core.domain.factory.FactoryMultiplierFilterDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryMultiplierFilterDuplicateService extends EntityDuplicateService<FactoryMultiplierFilterDuplicate, TwinFactoryMultiplierFilterEntity, TwinFactoryMultiplierEntity> {

    @Lazy
    private final FactoryMultiplierFilterService factoryMultiplierFilterService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryMultiplierFilterEntity> entityService() {
        return factoryMultiplierFilterService;
    }

    @Override
    protected FactoryMultiplierFilterDuplicate createNewDuplicate() {
        return new FactoryMultiplierFilterDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinFactoryMultiplierEntity> parents) {
        factoryMultiplierFilterService.loadFactoryMultiplierFilters(parents);
    }

    @Override
    protected Kit<TwinFactoryMultiplierFilterEntity, UUID> extractorChildren(TwinFactoryMultiplierEntity parent) {
        return parent.getTwinFactoryMultiplierFilterKit();
    }

    @Override
    protected UUID extractParentId(TwinFactoryMultiplierEntity parent) {
        return parent.getId();
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void validateKeyUniqueness(Collection<FactoryMultiplierFilterDuplicate> duplicates) throws ServiceException {
        // multiplier filters have no key concept
    }

    @Override
    protected TwinFactoryMultiplierFilterEntity createNewEntity(FactoryMultiplierFilterDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryMultiplierFilterEntity()
                .setTwinFactoryMultiplierId(src.getTwinFactoryMultiplierId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.isTwinFactoryConditionInvert())
                .setActive(src.isActive())
                .setDescription(src.getDescription());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryMultiplierFilterEntity src, TwinFactoryMultiplierFilterEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntity(TwinFactoryMultiplierFilterEntity newEntity, TwinFactoryMultiplierEntity parentEntity) {
        newEntity
                .setTwinFactoryMultiplierId(parentEntity.getId())
                .setMultiplier(parentEntity);
    }

    @Override
    protected void remapReferences(TwinFactoryMultiplierFilterEntity newEntity, EntityDuplicateContext ctx) {
        newEntity.setTwinFactoryConditionSetId(
                ctx.resolveOrDefault(TwinFactoryConditionSetEntity.class, newEntity.getTwinFactoryConditionSetId()));
    }
}
