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
import org.twins.core.domain.factory.FactoryDuplicate;
import org.twins.core.domain.factory.FactoryPipelineDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.*;

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
    @Lazy
    private final FactoryDuplicateService factoryDuplicateService;

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
        factoryPipelineService.loadNextTwinFactory(originalEntities);
        factoryPipelineService.loadAfterCommitTwinFactory(originalEntities);
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
        if (newConditionSetId != null && !Objects.equals(src.getTwinFactoryId(), dstFactory.getId())) {
            newConditionSetId = factoryConditionSetDuplicateService.lookupOrCollect(src.getConditionSet(), dstFactory.getId(), duplicateCollector);
        }
        UUID newNextFactoryId = remapNextFactoryId(src, duplicateCollector);
        UUID newAfterCommitFactoryId = remapAfterCommitFactoryId(src, duplicateCollector);
        return new TwinFactoryPipelineEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(newConditionSetId)
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setOutputTwinStatusId(src.getOutputTwinStatusId())
                .setNextTwinFactoryId(newNextFactoryId)
                .setNextTwinFactoryLimitScope(src.getNextTwinFactoryLimitScope())
                .setAfterCommitTwinFactoryId(newAfterCommitFactoryId)
                .setTemplateTwinId(src.getTemplateTwinId())
                .setDescription(src.getDescription())
                .setActive(src.getActive());
    }

    /**
     * Remaps {@code nextTwinFactoryId} to the cascade-duplicated factory when
     * {@code duplicateNextFactoryCascade} is set on the owning factory; otherwise copies the original id.
     */
    private UUID remapNextFactoryId(TwinFactoryPipelineEntity src, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        UUID originalNextId = src.getNextTwinFactoryId();
        if (originalNextId == null) {
            return null;
        }
        FactoryDuplicate ownerDuplicate = (FactoryDuplicate) duplicateCollector.getEntry(
                new EntityDuplicateCollector.DuplicateKey(TwinFactoryEntity.class, src.getTwinFactoryId(), null));
        if (ownerDuplicate == null || !ownerDuplicate.isDuplicateNextFactoryCascade() || src.getNextTwinFactory() == null) {
            return originalNextId;
        }
        return factoryDuplicateService.lookupOrCollect(src.getNextTwinFactory(), null, duplicateCollector);
    }

    /**
     * Remaps {@code afterCommitTwinFactoryId} to the cascade-duplicated factory when
     * {@code duplicateAfterCommitFactory} is set on the owning factory; otherwise copies the original id.
     * Independent from {@code duplicateNextFactoryCascade} — the two flags are orthogonal.
     */
    private UUID remapAfterCommitFactoryId(TwinFactoryPipelineEntity src, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        UUID originalId = src.getAfterCommitTwinFactoryId();
        if (originalId == null) {
            return null;
        }
        FactoryDuplicate ownerDuplicate = (FactoryDuplicate) duplicateCollector.getEntry(
                new EntityDuplicateCollector.DuplicateKey(TwinFactoryEntity.class, src.getTwinFactoryId(), null));
        if (ownerDuplicate == null || !ownerDuplicate.isDuplicateAfterCommitFactory() || src.getAfterCommitTwinFactory() == null) {
            return originalId;
        }
        return factoryDuplicateService.lookupOrCollect(src.getAfterCommitTwinFactory(), null, duplicateCollector);
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
