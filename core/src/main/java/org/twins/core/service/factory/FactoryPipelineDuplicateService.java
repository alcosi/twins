package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.factory.FactoryPipelineDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineDuplicateService extends EntityDuplicateService<FactoryPipelineDuplicate, TwinFactoryPipelineEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryPipelineService factoryPipelineService;
    @Lazy
    private final FactoryPipelineStepDuplicateService factoryStepDuplicateService;


    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryPipelineEntity> entityService() {
        return factoryPipelineService;
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
    protected TwinFactoryPipelineEntity createNewEntity(FactoryPipelineDuplicate duplicate) throws ServiceException {
        var originalEntity = duplicate.getOriginalEntity();
        return new TwinFactoryPipelineEntity()
                .setTwinFactoryId(originalEntity.getTwinFactoryId())
                .setInputTwinClassId(originalEntity.getInputTwinClassId())
                .setTwinFactoryConditionSetId(originalEntity.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(originalEntity.getTwinFactoryConditionInvert())
                .setOutputTwinStatusId(originalEntity.getOutputTwinStatusId())
                .setNextTwinFactoryId(originalEntity.getNextTwinFactoryId())
                .setNextTwinFactoryLimitScope(originalEntity.getNextTwinFactoryLimitScope())
                .setAfterCommitTwinFactoryId(originalEntity.getAfterCommitTwinFactoryId())
                .setTemplateTwinId(originalEntity.getTemplateTwinId())
                .setDescription(originalEntity.getDescription())
                .setActive(originalEntity.getActive());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryPipelineEntity src, TwinFactoryPipelineEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntityId(TwinFactoryPipelineEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinFactoryId(duplicateParentEntityId);
    }

    @Override
    protected void afterSave(Collection<FactoryPipelineDuplicate> duplicates, Collection<TwinFactoryPipelineEntity> saved) throws ServiceException {
        Map<TwinFactoryPipelineEntity, TwinFactoryPipelineEntity> stepsMap = null;
        for (var duplicate : duplicates) {
            TwinFactoryPipelineEntity src = duplicate.getOriginalEntity();
            TwinFactoryPipelineEntity dst = duplicate.getNewEntity();
            if (duplicate.isDuplicateSteps()) {
                if (stepsMap == null) stepsMap = new HashMap<>();
                stepsMap.put(src, dst);
            }
        }
        if (stepsMap != null) {
            factoryStepDuplicateService.duplicateFor(stepsMap);
        }
    }
}
