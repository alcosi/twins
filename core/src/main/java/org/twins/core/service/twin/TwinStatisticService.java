package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.statistic.TwinStatisticEntity;
import org.twins.core.dao.statistic.TwinStatisticRepository;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.featurer.statistic.Statister;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;


@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatisticService extends EntitySecureFindServiceImpl<TwinStatisticEntity> {
    @Autowired
    private TwinStatisticRepository twinStatisticRepository;
    @Autowired
    private FeaturerService featurerService;

    @Override
    public CrudRepository<TwinStatisticEntity, UUID> entityRepository() {
        return twinStatisticRepository;
    }

    @Override
    public Function<TwinStatisticEntity, UUID> entityGetIdFunction() {
        return TwinStatisticEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinStatisticEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        //todo impl me check domain
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatisticEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Map<UUID, TwinStatisticProgressPercent> calcStatistic(UUID statisticId, Set<UUID> twinIdSet) throws Exception {
        TwinStatisticEntity statisticEntity = findEntitySafe(statisticId);
        Statister<?> statister = featurerService.getFeaturer(statisticEntity.getStatisterFeaturerId(), Statister.class);
        return (Map<UUID, TwinStatisticProgressPercent>) statister.getStatistic(twinIdSet, statisticEntity.getStatisterParams());
    }

}
