package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCode;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.domain.factory.FactoryBranchDuplicate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.EntityDuplicateService;

import java.util.Collection;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FactoryBranchDuplicateService extends EntityDuplicateService<FactoryBranchDuplicate, TwinFactoryBranchEntity, TwinFactoryEntity> {

    @Lazy
    private final FactoryBranchService factoryBranchService;

    @Override
    protected EntitySecureFindServiceImpl<TwinFactoryBranchEntity> entityService() {
        return factoryBranchService;
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
    protected TwinFactoryBranchEntity createNewEntity(FactoryBranchDuplicate duplicate) throws ServiceException {
        var src = duplicate.getOriginalEntity();
        return new TwinFactoryBranchEntity()
                .setTwinFactoryId(src.getTwinFactoryId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(src.getNextTwinFactoryId())
                .setDescription(src.getDescription());
    }

    @Override
    protected void duplicateI18nFields(TwinFactoryBranchEntity src, TwinFactoryBranchEntity dst) throws ServiceException {
        // no i18n fields
    }

    @Override
    protected void setNewParentEntityId(TwinFactoryBranchEntity newEntity, UUID duplicateParentEntityId) {
        newEntity.setTwinFactoryId(duplicateParentEntityId);
    }
}
