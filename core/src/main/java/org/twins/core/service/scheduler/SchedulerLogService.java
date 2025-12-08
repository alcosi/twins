package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerLogEntity;
import org.twins.core.dao.scheduler.SchedulerLogRepository;
import org.twins.core.dao.scheduler.SchedulerRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerLogService extends EntitySecureFindServiceImpl<SchedulerLogEntity> {

    private final SchedulerLogRepository schedulerLogRepository;
    private final SchedulerRepository schedulerRepository;

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
        return false;
    }

    @Override
    public void beforeValidateEntity(SchedulerLogEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        super.beforeValidateEntity(entity, entityValidateMode);
    }

    public void loadScheduler(SchedulerLogEntity entity) {
        loadSchedulers(List.of(entity));
    }

    public void loadSchedulers(Collection<SchedulerLogEntity> entities) {
        if (entities.isEmpty()) {
            return;
        }

        List<SchedulerLogEntity> needToLoad = entities.stream().filter((entity) -> entity.getScheduler() == null).toList();

        if (needToLoad.isEmpty()) {
            return;
        }

        Kit<SchedulerLogEntity, UUID> schedulerLogs = new Kit<>(needToLoad, SchedulerLogEntity::getSchedulerId);
        List<SchedulerEntity> schedulers = schedulerRepository.findAllById(needToLoad.stream().map(SchedulerLogEntity::getSchedulerId).collect(Collectors.toSet()));

        for (var scheduler : schedulers) {
            schedulerLogs.get(scheduler.getId()).setScheduler(scheduler);
        }
    }
}
