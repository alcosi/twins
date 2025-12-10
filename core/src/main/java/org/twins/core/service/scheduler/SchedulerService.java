package org.twins.core.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.dao.FeaturerEntity;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.scheduler.SchedulerEntity;
import org.twins.core.dao.scheduler.SchedulerRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SchedulerService extends EntitySecureFindServiceImpl<SchedulerEntity> {

    private final SchedulerRepository schedulerRepository;
    private final FeaturerService featurerService;

    @Override
    public CrudRepository<SchedulerEntity, UUID> entityRepository() {
        return schedulerRepository;
    }

    @Override
    public Function<SchedulerEntity, UUID> entityGetIdFunction() {
        return SchedulerEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(SchedulerEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(SchedulerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return false;
    }

    @Override
    public void beforeValidateEntity(SchedulerEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) {
        super.beforeValidateEntity(entity, entityValidateMode);
    }

    public void loadFeaturer(SchedulerEntity entity) {
        loadFeaturers(List.of(entity));
    }
    
    public void loadFeaturers(Collection<SchedulerEntity> entities) {
        if (entities.isEmpty()) {
            return;
        }

        List<SchedulerEntity> needToLoad = entities.stream().filter((entity) -> entity.getFeaturer() == null).toList();

        if (needToLoad.isEmpty()) {
            return;
        }

        Kit<SchedulerEntity, Integer> schedulers = new Kit<>(needToLoad, SchedulerEntity::getFeaturerId);
        List<FeaturerEntity> featurers = featurerService.findByIdIn(needToLoad.stream().map(SchedulerEntity::getFeaturerId).collect(Collectors.toSet()));

        for (var featurer : featurers) {
            schedulers.get(featurer.getId()).setFeaturer(featurer);
        }
    }
}
