package org.twins.core.service.factory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryBranchRepository;

import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class TwinFactoryBranchService extends EntitySecureFindServiceImpl<TwinFactoryBranchEntity> {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;

    public TwinFactoryBranchEntity createFactoryBranch(TwinFactoryBranchEntity branchEntity) {
        return twinFactoryBranchRepository.save(branchEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryBranchEntity updateFactoryBranch(TwinFactoryBranchEntity branchUpdate) throws ServiceException {
        TwinFactoryBranchEntity dbFactoryBranchEntity = findEntitySafe(branchUpdate.getId());
        branchUpdate.setTwinFactoryId(dbFactoryBranchEntity.getTwinFactoryId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateFactoryBranchConditionSetId(dbFactoryBranchEntity, branchUpdate.getTwinFactoryConditionSetId(), changesHelper);
        updateFactoryBranchConditionSerInvert(dbFactoryBranchEntity, branchUpdate.isTwinFactoryConditionInvert(), changesHelper);
        updateFactoryBranchActive(dbFactoryBranchEntity, branchUpdate.isActive(), changesHelper);
        updateFactoryBranchNextFactoryId(dbFactoryBranchEntity, branchUpdate.getNextTwinFactoryId(), changesHelper);
        updateFactoryBranchDescription(dbFactoryBranchEntity, branchUpdate.getDescription(), changesHelper);
        if (changesHelper.hasChanges())
            return entitySmartService.saveAndLogChanges(dbFactoryBranchEntity, twinFactoryBranchRepository, changesHelper);
        return dbFactoryBranchEntity;
    }

    private void updateFactoryBranchConditionSetId(TwinFactoryBranchEntity dbFactoryBranchEntity, UUID twinFactoryConditionSetId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId, dbFactoryBranchEntity.getTwinFactoryConditionSetId(), twinFactoryConditionSetId))
            return;
        dbFactoryBranchEntity.setTwinFactoryConditionSetId(UuidUtils.nullifyIfNecessary(twinFactoryConditionSetId));
    }

    private void updateFactoryBranchConditionSerInvert(TwinFactoryBranchEntity dbFactoryBranchEntity, boolean twinFactoryConditionInvert, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.twinFactoryConditionInvert, dbFactoryBranchEntity.isTwinFactoryConditionInvert(), twinFactoryConditionInvert))
            return;
        dbFactoryBranchEntity.setTwinFactoryConditionInvert(twinFactoryConditionInvert);
    }

    private void updateFactoryBranchActive(TwinFactoryBranchEntity dbFactoryBranchEntity, boolean active, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.active, dbFactoryBranchEntity.isActive(), active))
            return;
        dbFactoryBranchEntity.setActive(active);
    }

    private void updateFactoryBranchNextFactoryId(TwinFactoryBranchEntity dbFactoryBranchEntity, UUID nextTwinFactoryId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.nextTwinFactoryId, dbFactoryBranchEntity.getNextTwinFactoryId(), nextTwinFactoryId))
            return;
        dbFactoryBranchEntity.setNextTwinFactoryId(UuidUtils.nullifyIfNecessary(nextTwinFactoryId));
    }

    private void updateFactoryBranchDescription(TwinFactoryBranchEntity dbFactoryBranchEntity, String description, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.description, dbFactoryBranchEntity.getDescription(), description))
            return;
        dbFactoryBranchEntity.setDescription(description);
    }

    @Override
    public CrudRepository<TwinFactoryBranchEntity, UUID> entityRepository() {
        return twinFactoryBranchRepository;
    }

    @Override
    public Function<TwinFactoryBranchEntity, UUID> entityGetIdFunction() {
        return TwinFactoryBranchEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinFactoryBranchEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinFactoryBranchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
