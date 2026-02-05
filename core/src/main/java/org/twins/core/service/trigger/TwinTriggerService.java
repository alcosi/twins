package org.twins.core.service.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.trigger.TwinTriggerEntity;
import org.twins.core.dao.trigger.TwinTriggerRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskEntity;
import org.twins.core.dao.trigger.TwinTriggerTaskRepository;
import org.twins.core.dao.trigger.TwinTriggerTaskStatus;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@AllArgsConstructor
public class TwinTriggerService extends EntitySecureFindServiceImpl<TwinTriggerEntity> {
    @Getter
    private final TwinTriggerRepository repository;
    private final TwinTriggerTaskRepository twinTriggerTaskRepository;
    private final AuthService authService;

    @Override
    public CrudRepository<TwinTriggerEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinTriggerEntity, UUID> entityGetIdFunction() {
        return TwinTriggerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinTriggerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        // TODO: implement domain check if needed
        return false;
    }

    @Override
    public boolean validateEntity(TwinTriggerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity, EntitySmartService.ReadPermissionCheckMode.none);
    }

    public void addTasks(Collection<TwinTriggerTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<TwinTriggerTaskEntity> triggerTaskList = new ArrayList<>();
        for (var task : tasks) {
            task
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId())
                    .setBusinessAccountId(apiUser.getBusinessAccountId());
            if (task.getStatusId() == null)
                task.setStatusId(TwinTriggerTaskStatus.NEED_START);
            triggerTaskList.add(task);
        }
        entitySmartService.saveAllAndLog(triggerTaskList, twinTriggerTaskRepository);
    }
}
