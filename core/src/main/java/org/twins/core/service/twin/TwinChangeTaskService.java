package org.twins.core.service.twin;

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
import org.twins.core.dao.TwinChangeTaskStatus;
import org.twins.core.dao.domain.DomainEntity;
import org.twins.core.dao.twin.TwinChangeTaskEntity;
import org.twins.core.dao.twin.TwinChangeTaskRepository;
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
public class TwinChangeTaskService extends EntitySecureFindServiceImpl<TwinChangeTaskEntity> {
    @Getter
    private final TwinChangeTaskRepository repository;
    private final AuthService authService;

    @Override
    public CrudRepository<TwinChangeTaskEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinChangeTaskEntity, UUID> entityGetIdFunction() {
        return TwinChangeTaskEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinChangeTaskEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        DomainEntity domain = authService.getApiUser().getDomain();
        boolean readDenied=!entity.getTwin().getTwinClass().getDomainId().equals(domain.getId());
        if (readDenied) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, domain.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in domain[" + domain.easyLog(EasyLoggable.Level.NORMAL));
        }
        return readDenied;
    }

    @Override
    public boolean validateEntity(TwinChangeTaskEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return !isEntityReadDenied(entity,EntitySmartService.ReadPermissionCheckMode.none);
    }

    public void addTasks(Collection<TwinChangeTaskEntity> tasks) throws ServiceException {
        if (CollectionUtils.isEmpty(tasks))
            return;
        ApiUser apiUser = authService.getApiUser();
        List<TwinChangeTaskEntity> changeTaskList = new ArrayList<>();
        for (var task : tasks) {
            task
                    .setRequestId(apiUser.getRequestId()) //we have uniq index on twinId + requestId to avoid conflict runs
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUserId())
                    .setBusinessAccountId(apiUser.getBusinessAccountId());
            if (task.getStatusId() == null)
                task.setStatusId(TwinChangeTaskStatus.NEED_START);
            changeTaskList.add(task);
        }
        entitySmartService.saveAllAndLog(changeTaskList, repository);
    }
}
