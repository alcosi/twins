package org.twins.core.service.factory;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.UuidUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dao.factory.TwinFactoryBranchRepository;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.i18n.I18nService;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.KitUtils;
import org.twins.core.domain.factory.FactoryBranchDuplicate;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@Slf4j
@RequiredArgsConstructor
public class FactoryBranchService extends EntitySecureFindServiceImpl<TwinFactoryBranchEntity> {
    private final TwinFactoryBranchRepository twinFactoryBranchRepository;
    private final AuthService authService;
    @Lazy
    private final TwinFactoryService twinFactoryService;
    @Lazy
    private final I18nService i18nService;
    @Lazy
    private final FactoryConditionSetService factoryConditionSetService;

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

    public void duplicateBranchesForFactory(TwinFactoryEntity fromFactory, TwinFactoryEntity toFactory) throws ServiceException {
        List<TwinFactoryBranchEntity> branches = fromFactory.getTwinFactoryBranchKit().getList();
        if (CollectionUtils.isEmpty(branches)) {
            return;
        }
        var entitiesForSave = new ArrayList<TwinFactoryBranchEntity>();
        for (TwinFactoryBranchEntity originalBranch : branches) {
            TwinFactoryBranchEntity duplicateBranch = new TwinFactoryBranchEntity()
                    .setTwinFactoryId(toFactory.getId())
                    .setTwinFactoryConditionSetId(originalBranch.getTwinFactoryConditionSetId())
                    .setTwinFactoryConditionInvert(originalBranch.getTwinFactoryConditionInvert())
                    .setActive(originalBranch.getActive())
                    .setNextTwinFactoryId(originalBranch.getNextTwinFactoryId())
                    .setDescription(originalBranch.getDescription());
            entitiesForSave.add(duplicateBranch);
        }
        saveSafe(entitiesForSave);
    }

    @Transactional
    public Collection<TwinFactoryBranchEntity> duplicateBranches(Collection<FactoryBranchDuplicate> duplicates) throws ServiceException {
        if (CollectionUtils.isEmpty(duplicates)) {
            return Collections.emptyList();
        }
        loadOriginalBranches(duplicates);
        for (var duplicate : duplicates) {
            if (duplicate.getNewTwinFactoryId() == null) {
                duplicate.setNewTwinFactoryId(duplicate.getOriginalFactoryBranch().getTwinFactoryId());
            }
        }
        var entitiesForSave = new ArrayList<TwinFactoryBranchEntity>();
        for (var duplicate : duplicates) {
            TwinFactoryBranchEntity duplicateBranch = duplicateBranchEntity(duplicate.getOriginalFactoryBranch(), duplicate.getNewTwinFactoryId());
            entitiesForSave.add(duplicateBranch);
        }
        return StreamSupport.stream(saveSafe(entitiesForSave).spliterator(), false).toList();
    }

    private void loadOriginalBranches(Collection<FactoryBranchDuplicate> duplicates) throws ServiceException {
        load(duplicates,
                FactoryBranchDuplicate::getOriginalFactoryBranchId,
                FactoryBranchDuplicate::getOriginalFactoryBranch,
                FactoryBranchDuplicate::setOriginalFactoryBranch);
    }

    private TwinFactoryBranchEntity duplicateBranchEntity(TwinFactoryBranchEntity srcBranchEntity, UUID newTwinFactoryId) throws ServiceException {
        return new TwinFactoryBranchEntity()
                .setTwinFactoryId(newTwinFactoryId)
                .setTwinFactoryConditionSetId(srcBranchEntity.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(srcBranchEntity.getTwinFactoryConditionInvert())
                .setActive(srcBranchEntity.getActive())
                .setNextTwinFactoryId(srcBranchEntity.getNextTwinFactoryId())
                .setDescription(srcBranchEntity.getDescription());
    }

    public void loadFactoryBranches(TwinFactoryEntity factory) {
        loadFactoryBranches(Collections.singletonList(factory));
    }

    public void loadFactoryBranches(Collection<TwinFactoryEntity> factories) {
        loadKit(
                factories,
                TwinFactoryEntity::getId,
                TwinFactoryEntity::getTwinFactoryBranchKit,
                TwinFactoryEntity::setTwinFactoryBranchKit,
                twinFactoryBranchRepository::findByTwinFactoryIdIn,
                TwinFactoryBranchEntity::getId,
                TwinFactoryBranchEntity::getTwinFactoryId);
    }
}
