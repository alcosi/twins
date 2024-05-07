package org.twins.core.featurer.twinclass.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.service.twin.TwinSearchResult;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 2602,
        name = "HeadHunterByStatus",
        description = "")
public class HeadHunterByStatus extends HeadHunter {
    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @FeaturerParam(name = "statusIds", description = "")
    public static final FeaturerParamUUIDSet statusIds = new FeaturerParamUUIDSet("statusIds");

    @FeaturerParam(name = "excludeStatusInput", description = "")
    public static final FeaturerParamBoolean excludeStatusInput = new FeaturerParamBoolean("excludeStatusInput");

    @Override
    protected TwinSearchResult findValidHead(Properties properties, TwinClassEntity twinClassEntity, Pageable pageable) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(twinClassEntity.getId());
        if (excludeStatusInput.extract(properties))
            search.addStatusId(statusIds.extract(properties));
        else
            search.addStatusIdExclude(statusIds.extract(properties));
        return twinSearchService.findTwins(search, (int) pageable.getOffset(), pageable.getPageSize());
    }
}
