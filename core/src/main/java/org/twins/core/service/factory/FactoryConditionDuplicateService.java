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
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryConditionDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryConditionDuplicateService extends EntityDuplicateService<FactoryConditionDuplicate, TwinFactoryConditionEntity, TwinFactoryConditionSetEntity> {

    @Lazy
    private final FactoryConditionService factoryConditionService;
    @Lazy
    private final FactoryConditionSetService factoryConditionSetService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryConditionEntity> entityService() {
        return factoryConditionService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryConditionSetEntity> entityParentService() {
        return factoryConditionSetService;
    }

    @Override
    protected Class<TwinFactoryConditionEntity> getEntityClass() {
        return TwinFactoryConditionEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinFactoryConditionSetEntity.class);
    }

    @Override
    protected FactoryConditionDuplicate createNewDuplicate() {
        return new FactoryConditionDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinFactoryConditionSetEntity> parents) {
        factoryConditionService.loadFactoryConditions(parents);
    }

    @Override
    protected Kit<TwinFactoryConditionEntity, UUID> extractorChildren(TwinFactoryConditionSetEntity parent) {
        return parent.getTwinFactoryConditionKit();
    }

    @Override
    protected UUID extractParentId(TwinFactoryConditionSetEntity parent) {
        return parent.getId();
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void validateKeyUniqueness(Collection<FactoryConditionDuplicate> duplicates) throws ServiceException {
        // conditions have no key concept
    }

    @Override
    protected TwinFactoryConditionEntity createNewEntity(FactoryConditionDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryConditionEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setConditionerFeaturerId(src.getConditionerFeaturerId())
                .setConditionerParams(src.getConditionerParams())
                .setDescription(src.getDescription())
                .setActive(src.getActive())
                .setInvert(src.getInvert());
    }

    @Override
    protected void setNewParentEntity(TwinFactoryConditionEntity newEntity, TwinFactoryConditionSetEntity parentEntity) {
        newEntity
                .setTwinFactoryConditionSetId(parentEntity.getId())
                .setConditionSet(parentEntity);
    }
}
