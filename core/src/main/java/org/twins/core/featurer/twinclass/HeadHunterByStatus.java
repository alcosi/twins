package org.twins.core.featurer.twinclass;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.pagination.PaginationResult;
import org.cambium.common.pagination.SimplePagination;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsStatusId;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2602,
        name = "HeadHunterByStatus",
        description = "")
public class HeadHunterByStatus extends HeadHunter {
    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSetTwinsStatusId("statusIds");

    @FeaturerParam(name = "excludeStatusInput", description = "")
    public static final FeaturerParamBoolean excludeStatusInput = new FeaturerParamBoolean("excludeStatusInput");

    @Override
    protected PaginationResult<TwinEntity> findValidHead(Properties properties, TwinClassEntity twinClassEntity, SimplePagination pagination) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(twinClassEntity.getId(), false)
                .addStatusId(statusIds.extract(properties), excludeStatusInput.extract(properties));
        return twinSearchService.findTwins(search, pagination);
    }

    @Override
    protected boolean isCreatableChildClass(Properties properties, TwinEntity twinEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        if (excludeStatusInput.extract(properties))
            return !statusIds.extract(properties).contains(twinEntity.getTwinStatusId());
        else
            return statusIds.extract(properties).contains(twinEntity.getTwinStatusId());
    }
}
