package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinRepository;
import org.twins.core.domain.Item;
import org.twins.core.domain.TwinStatistic;
import org.twins.core.domain.TwinStatisticProgressPercent;

import java.util.*;
import java.util.function.Function;


@Lazy
@Slf4j
@Service
@RequiredArgsConstructor
public class TwinStatisticService extends EntitySecureFindServiceImpl<TwinEntity> {

    private final TwinRepository twinRepository;

    @Override
    public CrudRepository<TwinEntity, UUID> entityRepository() {
        return twinRepository;
    }

    @Override
    public Function<TwinEntity, UUID> entityGetIdFunction() {
        return TwinEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public Map<UUID, TwinStatistic> calcStatistic(UUID statisticId, Set<UUID> twinIdSet) throws Exception {
        //todo mock object
        Item item = new Item()
                .setLabel("In progress")
                .setKey("inProgress")
                .setPercent(30)
                .setColorHex("#22FF00");
        TwinStatistic statistic = new TwinStatisticProgressPercent()
                .setItems(List.of(item));
        Map<UUID, TwinStatistic> statisticMap = new HashMap<>();
        for (UUID uuid : twinIdSet) {
            statisticMap.put(uuid, statistic);
        }
        return statisticMap;
    }

}
