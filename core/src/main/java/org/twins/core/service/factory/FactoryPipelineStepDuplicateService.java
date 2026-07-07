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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineStepEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryPipelineStepDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineStepDuplicateService extends EntityDuplicateService<FactoryPipelineStepDuplicate, TwinFactoryPipelineStepEntity, TwinFactoryPipelineEntity> {

    @Lazy
    private final FactoryPipelineStepService factoryPipelineStepService;
    @Lazy
    private final FactoryPipelineService factoryPipelineService;
    @Lazy
    private final FactoryConditionSetService factoryConditionSetService;
    @Lazy
    private final FactoryConditionSetDuplicateService conditionSetDuplicateService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryPipelineStepEntity> entityService() {
        return factoryPipelineStepService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryPipelineEntity> entityParentService() {
        return factoryPipelineService;
    }

    @Override
    protected Class<TwinFactoryPipelineStepEntity> getEntityClass() {
        return TwinFactoryPipelineStepEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinFactoryPipelineEntity.class, TwinFactoryConditionSetEntity.class);
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
    protected void loadRequiredRelations(List<TwinFactoryPipelineStepEntity> originalEntities) throws ServiceException {
        factoryPipelineStepService.loadPipeline(originalEntities);
        factoryPipelineStepService.loadConditionSet(originalEntities);
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
    protected TwinFactoryPipelineStepEntity createNewEntity(FactoryPipelineStepDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        var srcPipeLine = duplicate.getOriginalEntity().getTwinFactoryPipeline();
        var dstPipeline = duplicate.getNewParentEntity();
        UUID newConditionSetId = src.getTwinFactoryConditionSetId();
        if (src.getTwinFactoryConditionSetId() != null && !srcPipeLine.getTwinFactoryId().equals(dstPipeline.getTwinFactoryId())) {
            newConditionSetId = conditionSetDuplicateService.lookupOrCollect(src.getTwinFactoryConditionSet(), dstPipeline.getTwinFactoryId(), duplicateCollector);
        }
        return new TwinFactoryPipelineStepEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryPipelineId(src.getTwinFactoryPipelineId())
                .setTwinFactoryConditionSetId(newConditionSetId)
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setOrder(src.getOrder())
                .setActive(src.getActive())
                .setOptional(src.getOptional())
                .setFillerFeaturerId(src.getFillerFeaturerId())
                .setFillerParams(src.getFillerParams())
                .setDescription(src.getDescription());
    }

    @Override
    protected void setNewParentEntity(TwinFactoryPipelineStepEntity newEntity, TwinFactoryPipelineEntity parentEntity) {
        newEntity
                .setTwinFactoryPipelineId(parentEntity.getId())
                .setTwinFactoryPipeline(parentEntity);
    }
}
