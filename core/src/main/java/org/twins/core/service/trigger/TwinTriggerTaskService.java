package org.twins.core.service.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.businessaccount.BusinessAccountService;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.user.UserService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

@Getter
@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class TwinTriggerTaskService extends EntitySecureFindServiceImpl<TwinTriggerTaskEntity> {
    private final TwinTriggerTaskRepository repository;
    private final AuthService authService;
    @Lazy
    private final TwinTriggerService twinTriggerService;
    private final BusinessAccountService businessAccountService;
    private final TwinStatusService twinStatusService;
    @Lazy
    private final TwinService twinService;
    @Lazy
    private final UserService userService;

    @Override
    public CrudRepository<TwinTriggerTaskEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinTriggerTaskEntity, UUID> entityGetIdFunction() {
        return TwinTriggerTaskEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinTriggerTaskEntity entity, org.cambium.service.EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinTriggerTaskEntity entity, org.cambium.service.EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity, org.cambium.service.EntitySmartService.ReadPermissionCheckMode.none);
    }

    public void addTasks(Collection<TwinTriggerTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks))
            return;
        log.info("Adding {} twin trigger tasks", tasks.size());
        ApiUser apiUser = authService.getApiUser();
        for (var task : tasks) {
            task
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId())
                    .setBusinessAccountId(apiUser.getBusinessAccountId());
            if (task.getStatusId() == null)
                task.setStatusId(TwinTriggerTaskStatus.NEED_START);
        }
        entitySmartService.saveAllAndLog(tasks, repository);
    }

    public void loadBusinessAccount(TwinTriggerTaskEntity src) throws ServiceException {
        loadBusinessAccounts(Collections.singletonList(src));
    }

    public void loadBusinessAccounts(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        businessAccountService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getBusinessAccountId,
                TwinTriggerTaskEntity::getBusinessAccount,
                TwinTriggerTaskEntity::setBusinessAccount);
    }

    public void loadTwinTrigger(TwinTriggerTaskEntity src) throws ServiceException {
        loadTwinTriggers(Collections.singletonList(src));
    }

    public void loadTwinTriggers(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        twinTriggerService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getTwinTriggerId,
                TwinTriggerTaskEntity::getTwinTrigger,
                TwinTriggerTaskEntity::setTwinTrigger);
    }

    public void loadCreatedByUser(TwinTriggerTaskEntity src) throws ServiceException {
        loadCreatedByUser(Collections.singletonList(src));
    }

    public void loadCreatedByUser(Collection<TwinTriggerTaskEntity> srcCollection) throws ServiceException {
        userService.load(srcCollection,
                TwinTriggerTaskEntity::getId,
                TwinTriggerTaskEntity::getCreatedByUserId,
                TwinTriggerTaskEntity::getCreatedByUser,
                TwinTriggerTaskEntity::setCreatedByUser);
    }

    public TwinTriggerTaskEntity addSyncTask(UUID twinId, UUID twinTriggerId, UUID previousTwinStatusId) throws ServiceException {
        log.info("Adding sync trigger task for twin[{}], trigger[{}]", twinId, twinTriggerId);
        ApiUser apiUser = authService.getApiUser();
        Timestamp now = Timestamp.from(Instant.now());
        TwinTriggerTaskEntity syncTask = new TwinTriggerTaskEntity()
                .setTwinId(twinId)
                .setTwinTriggerId(twinTriggerId)
                .setPreviousTwinStatusId(previousTwinStatusId)
                .setStatusId(TwinTriggerTaskStatus.SYNC_EXECUTION)
                .setCreatedAt(now)
                .setDoneAt(now)
                .setCreatedByUserId(apiUser.getUserId())
                .setBusinessAccountId(apiUser.getBusinessAccountId());
        return saveSafe(syncTask);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void runTrigger(TwinTriggerTaskEntity triggerTaskEntity, TwinStatusEntity dstTwinStatus) throws ServiceException {
        twinTriggerService.runTrigger(
                triggerTaskEntity.getTwinTrigger(),
                triggerTaskEntity.getTwin(),
                triggerTaskEntity.getPreviousTwinStatus(),
                dstTwinStatus,
                triggerTaskEntity.getId());
    }
}
