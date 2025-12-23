package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
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
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.statistic.TwinStatisticProgressPercent;
import org.twins.core.featurer.statistic.Statister;
import org.twins.core.service.auth.AuthService;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;


@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinStatisticService extends EntitySecureFindServiceImpl<TwinStatisticEntity> {
    @Autowired
    private TwinStatisticRepository twinStatisticRepository;
    @Autowired
    private FeaturerService featurerService;
    @Autowired
    private AuthService authService;

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
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomain().getId())) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entity.easyLog(EasyLoggable.Level.NORMAL) + " is not allowed in " + apiUser.getDomain().logNormal());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateEntity(TwinStatisticEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Map<UUID, TwinStatisticProgressPercent> calcStatisticProgressPercent(UUID statisticId, Set<UUID> twinIdSet) throws Exception {
        TwinStatisticEntity statisticEntity = findEntitySafe(statisticId);
        Statister<?> statister = featurerService.getFeaturer(statisticEntity.getStatisterFeaturerId(), Statister.class);
        return (Map<UUID, TwinStatisticProgressPercent>) statister.getStatistic(statisticEntity.getStatisterParams(), twinIdSet);
    }

}
