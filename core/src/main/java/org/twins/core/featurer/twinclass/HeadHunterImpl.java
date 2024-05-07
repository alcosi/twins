package org.twins.core.featurer.twinclass;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
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
@Featurer(id = 2601,
        name = "HeadHunterImpl",
        description = "")
public class HeadHunterImpl extends HeadHunter {
    @Lazy
    @Autowired
    TwinSearchService twinSearchService;

    @Override
    protected TwinSearchResult findValidHead(Properties properties, TwinClassEntity twinClassEntity, Pageable pageable) throws ServiceException {
        BasicSearch search = new BasicSearch();
        search
                .addTwinClassId(twinClassEntity.getId());
        return twinSearchService.findTwins(search, (int) pageable.getOffset(), pageable.getPageSize());
    }
}
