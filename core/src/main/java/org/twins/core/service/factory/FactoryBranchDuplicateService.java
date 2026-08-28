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
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.EntityDuplicateCollector;
import org.twins.core.domain.factory.FactoryBranchDuplicate;
import org.twins.core.domain.factory.FactoryDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryBranchDuplicateService extends EntityDuplicateService<FactoryBranchDuplicate, TwinFactoryBranchEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryBranchService factoryBranchService;
    @Lazy
    private final FactoryService factoryService;
    @Lazy
    private final FactoryConditionSetDuplicateService factoryConditionSetDuplicateService;
    @Lazy
    private final FactoryDuplicateService factoryDuplicateService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryBranchEntity> entityService() {
        return factoryBranchService;
    }

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryEntity> entityParentService() {
        return factoryService;
    }

    @Override
    protected Class<TwinFactoryBranchEntity> getEntityClass() {
        return TwinFactoryBranchEntity.class;
    }

    @Override
    protected Set<Class<?>> commitAfter() {
        return Set.of(TwinFactoryEntity.class, TwinFactoryConditionSetEntity.class);
    }

    @Override
    protected void loadRequiredRelations(List<TwinFactoryBranchEntity> originalEntities) throws ServiceException {
        factoryBranchService.loadConditionSet(originalEntities);
        factoryBranchService.loadNextFactory(originalEntities);
    }

    @Override
    protected FactoryBranchDuplicate createNewDuplicate() {
        return new FactoryBranchDuplicate();
    }

    @Override
    protected void loadFor(Collection<TwinFactoryEntity> parents) {
        factoryBranchService.loadFactoryBranches(parents);
    }

    @Override
    protected Kit<TwinFactoryBranchEntity, UUID> extractorChildren(TwinFactoryEntity parent) {
        return parent.getTwinFactoryBranchKit();
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
    protected void validateKeyUniqueness(Collection<FactoryBranchDuplicate> duplicates) throws ServiceException {
        // branches have no key concept
    }

    @Override
    protected TwinFactoryBranchEntity createNewEntity(FactoryBranchDuplicate duplicate, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        UUID targetFactoryId = duplicate.getNewParentEntity().getId();
        UUID newConditionSetId = src.getTwinFactoryConditionSetId();
        if (newConditionSetId != null && !Objects.equals(src.getTwinFactoryId(), targetFactoryId)) {
            newConditionSetId = factoryConditionSetDuplicateService.lookupOrCollect(src.getTwinFactoryConditionSet(), targetFactoryId, duplicateCollector);
        }
        UUID newNextFactoryId = remapNextFactoryId(src, duplicateCollector);
        return new TwinFactoryBranchEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setTwinFactoryConditionSetId(newConditionSetId)
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(newNextFactoryId)
                .setDescription(src.getDescription());
    }

    /**
     * When {@code duplicateNextFactoryCascade} is set on the owning factory, the branch's
     * {@code nextTwinFactoryId} is remapped to the cascade-duplicated factory (created on demand via
     * {@code lookupOrCollect}); otherwise the original id is copied verbatim. The owning
     * {@link FactoryDuplicate} is read from the collector registry (it is registered before the
     * branch cascade runs).
     */
    private UUID remapNextFactoryId(TwinFactoryBranchEntity src, EntityDuplicateCollector duplicateCollector) throws ServiceException {
        UUID originalNextId = src.getNextTwinFactoryId();
        if (originalNextId == null) {
            return null;
        }
        FactoryDuplicate ownerDuplicate = (FactoryDuplicate) duplicateCollector.getEntry(
                new EntityDuplicateCollector.DuplicateKey(TwinFactoryEntity.class, src.getTwinFactoryId(), null));
        if (ownerDuplicate == null || !ownerDuplicate.isDuplicateNextFactoryCascade() || src.getNextFactory() == null) {
            return originalNextId;
        }
        return factoryDuplicateService.lookupOrCollect(src.getNextFactory(), null, duplicateCollector);
    }

    @Override
    protected void setNewParentEntity(TwinFactoryBranchEntity newEntity, TwinFactoryEntity parentEntity) {
        newEntity
                .setTwinFactoryId(parentEntity.getId())
                .setFactory(parentEntity);
    }
}
