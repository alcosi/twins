package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dao.factory.TwinFactoryPipelineEntity;
import org.twins.core.domain.factory.FactoryPipelineDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryPipelineDuplicateService extends EntityDuplicateService<FactoryPipelineDuplicate, TwinFactoryPipelineEntity> {

    @Lazy
    private final FactoryPipelineService factoryPipelineService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryPipelineEntity> entityService() {
        return factoryPipelineService;
    }

    @Override
    protected FactoryPipelineDuplicate createNewDuplicate() {
        return new FactoryPipelineDuplicate();
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

    public void duplicateForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory) throws ServiceException {
        List<TwinFactoryPipelineEntity> pipelines = fromFactory.getTwinFactoryPipelineKit().getList();
        if (pipelines == null || pipelines.isEmpty()) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryPipelineEntity>();
        for (TwinFactoryPipelineEntity originalPipeline : pipelines) {
            TwinFactoryPipelineEntity duplicatePipeline = new TwinFactoryPipelineEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setInputTwinClassId(originalPipeline.getInputTwinClassId())
                    .setTwinFactoryConditionSetId(originalPipeline.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(originalPipeline.getTwinFactoryConditionInvert())
                    .setOutputTwinStatusId(originalPipeline.getOutputTwinStatusId())
                    .setNextTwinFactoryId(originalPipeline.getNextTwinFactoryId())
                    .setNextTwinFactoryLimitScope(originalPipeline.getNextTwinFactoryLimitScope())
                    .setAfterCommitTwinFactoryId(originalPipeline.getAfterCommitTwinFactoryId())
                    .setTemplateTwinId(originalPipeline.getTemplateTwinId())
                    .setDescription(originalPipeline.getDescription())
                    .setActive(originalPipeline.getActive());
            entitiesForSave.add(duplicatePipeline);
        }
        factoryPipelineService.saveSafe(entitiesForSave);
    }
}
