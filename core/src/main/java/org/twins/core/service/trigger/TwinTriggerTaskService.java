package org.twins.core.service.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.ArrayList;
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
}
