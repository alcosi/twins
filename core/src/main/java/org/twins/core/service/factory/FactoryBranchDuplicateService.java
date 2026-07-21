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
        return new TwinFactoryBranchEntity()
                .setId(UuidUtils.generate())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setTwinFactoryConditionSetId(newConditionSetId)
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(src.getNextTwinFactoryId())
                .setDescription(src.getDescription());
    }

    @Override
    protected void setNewParentEntity(TwinFactoryBranchEntity newEntity, TwinFactoryEntity parentEntity) {
        newEntity
                .setTwinFactoryId(parentEntity.getId())
                .setFactory(parentEntity);
    }
}
