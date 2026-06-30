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
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryPipelineDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineDuplicateService extends EntityDuplicateService<FactoryPipelineDuplicate, TwinFactoryPipelineEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryPipelineService factoryPipelineService;
    @Lazy
    private final FactoryPipelineStepDuplicateService factoryStepDuplicateService;
    @Lazy
    private final FactoryService factoryService;
    @Lazy
    private final FactoryConditionSetDuplicateService factoryConditionSetDuplicateService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryPipelineEntity> entityService() {
        return factoryPipelineService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityParentService() {
        return factoryService;
    }

    @Override
    protected Class<TwinFactoryPipelineEntity> getEntityClass() {
        return TwinFactoryPipelineEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinFactoryEntity.class, TwinFactoryConditionSetEntity.class);
    }

    @Override
    protected FactoryPipelineDuplicate createNewDuplicate() {
        return new FactoryPipelineDuplicate()
                .setDuplicateSteps(true);
    }

    @Override
    protected void loadFor(Collection<TwinFactoryEntity> parents) {
        factoryPipelineService.loadFactoryPipelines(parents);
    }

    @Override
    protected void loadRequiredRelations(List<TwinFactoryPipelineEntity> originalEntities) throws ServiceException {
        factoryPipelineService.loadConditionSet(originalEntities);
    }

    @Override
    protected Kit<TwinFactoryPipelineEntity, UUID> extractorChildren(TwinFactoryEntity parent) {
        return parent.getTwinFactoryPipelineKit();
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
    protected void validateKeyUniqueness(Collection<FactoryPipelineDuplicate> duplicates) throws ServiceException {
        // pipelines have no key concept
    }

    @Override
    protected TwinFactoryPipelineEntity createNewEntity(FactoryPipelineDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        var dstFactory = duplicate.getNewParentEntity();
        UUID newConditionSetId = src.getTwinFactoryConditionSetId();
        if (newConditionSetId != null && src.getTwinFactoryId() != dstFactory.getId()) {
            newConditionSetId = factoryConditionSetDuplicateService.lookupOrCollect(src.getConditionSet(), dstFactory.getId(), duplicateCollector);
        }
        return new TwinFactoryPipelineEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(newConditionSetId)
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setOutputTwinStatusId(src.getOutputTwinStatusId())
                .setNextTwinFactoryId(src.getNextTwinFactoryId())
                .setNextTwinFactoryLimitScope(src.getNextTwinFactoryLimitScope())
                .setAfterCommitTwinFactoryId(src.getAfterCommitTwinFactoryId())
                .setTemplateTwinId(src.getTemplateTwinId())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }

    @Override
    protected void setNewParentEntity(TwinFactoryPipelineEntity newEntity, TwinFactoryEntity parentEntity) {
        newEntity
                .setTwinFactoryId(parentEntity.getId())
                .setTwinFactory(parentEntity);
    }

    @Override
    protected List<ChildCascade<FactoryPipelineDuplicate, TwinFactoryPipelineEntity>> childCascades() {
        return List.of(
                new ChildCascade<>(FactoryPipelineDuplicate::isDuplicateSteps, factoryStepDuplicateService)
        );
    }
}
