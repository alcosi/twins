package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
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
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryBranchRepository;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.UUID;
import java.util.function.Function;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class FactoryBranchService extends EntitySecureFindServiceImpl<TwinFactoryBranchEntity> {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;
    private final AuthService authService;

    public TwinFactoryBranchEntity createFactoryBranch(TwinFactoryBranchEntity branchEntity) throws ServiceException {
        return saveSafe(branchEntity);
    }

    @Transactional(rollbackFor = Throwable.class)
    public TwinFactoryBranchEntity updateFactoryBranch(TwinFactoryBranchEntity branchUpdate) throws ServiceException {
        TwinFactoryBranchEntity dbFactoryBranchEntity = findEntitySafe(branchUpdate.getId());
        branchUpdate.setTwinFactoryId(dbFactoryBranchEntity.getTwinFactoryId());
        ChangesHelper changesHelper = new ChangesHelper();
        updateFactoryBranchConditionSetId(dbFactoryBranchEntity, branchUpdate.getTwinFactoryConditionSetId(), changesHelper);
        updateFactoryBranchConditionSerInvert(dbFactoryBranchEntity, branchUpdate.getTwinFactoryConditionInvert(), changesHelper);
        updateFactoryBranchActive(dbFactoryBranchEntity, branchUpdate.getActive(), changesHelper);
        updateFactoryBranchNextFactoryId(dbFactoryBranchEntity, branchUpdate.getNextTwinFactoryId(), changesHelper);
        updateFactoryBranchDescription(dbFactoryBranchEntity, branchUpdate.getDescription(), changesHelper);
        return updateSafe(dbFactoryBranchEntity, changesHelper);
    }

    private void updateFactoryBranchConditionSetId(TwinFactoryBranchEntity dbFactoryBranchEntity, UUID twinFactoryConditionSetId, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.twinFactoryConditionSetId, dbFactoryBranchEntity.getTwinFactoryConditionSetId(), twinFactoryConditionSetId))
            return;
        dbFactoryBranchEntity.setTwinFactoryConditionSetId(UuidUtils.nullifyIfNecessary(twinFactoryConditionSetId));
    }

    private void updateFactoryBranchConditionSerInvert(TwinFactoryBranchEntity dbFactoryBranchEntity, Boolean twinFactoryConditionInvert, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.twinFactoryConditionInvert, dbFactoryBranchEntity.getTwinFactoryConditionInvert(), twinFactoryConditionInvert))
            return;
        dbFactoryBranchEntity.setTwinFactoryConditionInvert(twinFactoryConditionInvert);
    }

    private void updateFactoryBranchActive(TwinFactoryBranchEntity dbFactoryBranchEntity, Boolean active, ChangesHelper changesHelper) {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.active, dbFactoryBranchEntity.getActive(), active))
            return;
        dbFactoryBranchEntity.setActive(active);
    }

    private void updateFactoryBranchNextFactoryId(TwinFactoryBranchEntity dbFactoryBranchEntity, UUID nextTwinFactoryId, ChangesHelper changesHelper) throws ServiceException {
        if (!changesHelper.isChanged(TwinFactoryBranchEntity.Fields.nextTwinFactoryId, dbFactoryBranchEntity.getNextTwinFactoryId(), nextTwinFactoryId))
            return;
        if (UuidUtils.isNullifyMarker(nextTwinFactoryId))
            throw new ServiceException(ErrorCodeTwins.UUID_NOT_BE_NULLIFY_MARKER);
        dbFactoryBranchEntity.setNextTwinFactoryId(nextTwinFactoryId);
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
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied = !entity.getFactory().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.logShort() + " is not allowed in " + domain.logShort());
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinFactoryBranchEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }
}
