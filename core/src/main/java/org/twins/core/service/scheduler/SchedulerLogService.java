package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dao.scheduler.SchedulerLogRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerLogService extends EntitySecureFindServiceImpl<SchedulerLogEntity> {

    private final SchedulerLogRepository schedulerLogRepository;
    private final SchedulerService schedulerService;

    @Override
    public CrudRepository<SchedulerLogEntity, UUID> entityRepository() {
        return schedulerLogRepository;
    }

    @Override
    public Function<SchedulerLogEntity, UUID> entityGetIdFunction() {
        return SchedulerLogEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SchedulerLogEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(SchedulerLogEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    @Override
    public void beforeValidateEntity(SchedulerLogEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        super.beforeValidateEntity(entity, entityValidateMode);
    }

    public void loadScheduler(SchedulerLogEntity entity) throws ServiceException {
        loadSchedulers(List.of(entity));
    }

    public void loadSchedulers(Collection<SchedulerLogEntity> entities) throws ServiceException {
        schedulerService.load(
                entities,
                SchedulerLogEntity::getId,
                SchedulerLogEntity::getSchedulerId,
                SchedulerLogEntity::getScheduler,
                SchedulerLogEntity::setScheduler
        );
    }
}
