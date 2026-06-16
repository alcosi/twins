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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.domain.EntityDuplicateContext;
import org.twins.core.domain.factory.FactoryPipelineStepDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineStepDuplicateService extends EntityDuplicateService<FactoryPipelineStepDuplicate, TwinFactoryPipelineStepEntity, TwinFactoryPipelineEntity> {

    @Lazy
    private final FactoryPipelineStepService factoryPipelineStepService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryPipelineStepEntity> entityService() {
        return factoryPipelineStepService;
    }

    @Override
    protected FactoryPipelineStepDuplicate createNewDuplicate() {
        return new FactoryPipelineStepDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinFactoryPipelineEntity> parents) {
        factoryPipelineStepService.loadFactoryPipelineSteps(parents);
    }

    @Override
    protected Kit<TwinFactoryPipelineStepEntity, UUID> extractorChildren(TwinFactoryPipelineEntity parent) {
        return parent.getTwinFactoryPipelineStepKit();
    }

    @Override
    protected UUID extractParentId(TwinFactoryPipelineEntity parent) {
        return parent.getId();
    }

    @Override
    protected ErrorCode getKeyDuplicatedErrorCode() {
        return ErrorCodeTwins.FACTORY_KEY_ALREADY_IN_USE;
    }

    @Override
    protected void validateKeyUniqueness(Collection<FactoryPipelineStepDuplicate> duplicates) throws ServiceException {
        // pipeline steps have no key concept
    }

    @Override
    protected TwinFactoryPipelineStepEntity createNewEntity(FactoryPipelineStepDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryPipelineStepEntity()
                .setTwinFactoryPipelineId(src.getTwinFactoryPipelineId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setOrder(src.getOrder())
                .setActive(src.getActive())
                .setOptional(src.getOptional())
                .setFillerFeaturerId(src.getFillerFeaturerId())
                .setFillerParams(src.getFillerParams())
                .setDescription(src.getDescription());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryPipelineStepEntity src, TwinFactoryPipelineStepEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntityId(TwinFactoryPipelineStepEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinFactoryPipelineId(duplicateParentEntityId);
    }

    @Override
    protected void remapReferences(TwinFactoryPipelineStepEntity newEntity, EntityDuplicateContext ctx) {
        newEntity.setTwinFactoryConditionSetId(
                ctx.resolveOrDefault(TwinFactoryConditionSetEntity.class, newEntity.getTwinFactoryConditionSetId()));
    }
}
