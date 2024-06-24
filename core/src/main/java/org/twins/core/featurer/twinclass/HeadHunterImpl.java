package org.twins.core.featurer.twinclass;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.pagination.PaginationResult;
import org.twins.core.service.pagination.SimplePagination;
import org.twins.core.service.twin.TwinSearchService;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2601,
        name = "HeadHunterImpl",
        description = "")
public class HeadHunterImpl extends HeadHunter {
    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected PaginationResult<TwinEntity> findValidHead(Properties properties, TwinClassEntity twinClassEntity, SimplePagination pagination) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(twinClassEntity.getId(), false);
        return twinSearchService.findTwins(search, pagination);
    }
}
