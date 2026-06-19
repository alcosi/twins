package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryEraserDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryEraserDuplicateService extends EntityDuplicateService<FactoryEraserDuplicate, TwinFactoryEraserEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryEraserService factoryEraserService;
    @Lazy
    private final FactoryService factoryService;
    @Lazy
    private final FactoryConditionSetDuplicateService factoryConditionSetDuplicateService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEraserEntity> entityService() {
        return factoryEraserService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityParentService() {
        return factoryService;
    }

    @Override
    protected Class<TwinFactoryEraserEntity> getEntityClass() {
        return TwinFactoryEraserEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinFactoryEntity.class, TwinFactoryConditionSetEntity.class);
    }

    @Override
    protected FactoryEraserDuplicate createNewDuplicate() {
        return new FactoryEraserDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinFactoryEntity> parents) {
        factoryEraserService.loadFactoryErasers(parents);
    }

    @Override
    protected Kit<TwinFactoryEraserEntity, UUID> extractorChildren(TwinFactoryEntity parent) {
        return parent.getTwinFactoryEraserKit();
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
    protected void validateKeyUniqueness(Collection<FactoryEraserDuplicate> duplicates) throws ServiceException {
        // erasers have no key concept
    }

    @Override
    protected TwinFactoryEraserEntity createNewEntity(FactoryEraserDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        UUID targetFactoryId = duplicate.getNewParentEntity().getId();
        UUID newConditionSetId = src.getTwinFactoryConditionSetId();
        if (newConditionSetId != null && src.getTwinFactoryId() != targetFactoryId) {
            newConditionSetId = factoryConditionSetDuplicateService.lookupOrCollect(src.getConditionSet(), targetFactoryId, duplicateCollector);
        }
        return new TwinFactoryEraserEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(newConditionSetId)
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setEraserAction(src.getEraserAction())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryEraserEntity src, TwinFactoryEraserEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntity(TwinFactoryEraserEntity newEntity, TwinFactoryEntity parentEntity) {
        newEntity
                .setTwinFactoryId(parentEntity.getId())
                .setTwinFactory(parentEntity);
    }

    @Override
    protected void loadRequiredRelations(List<TwinFactoryEraserEntity> originalEntities) throws ServiceException {
        super.loadRequiredRelations(originalEntities);
        factoryEraserService.loadConditionSet(originalEntities);
    }
}
