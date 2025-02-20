package org.twins.core.featurer.headhunter;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_2601,
        name = "All",
        description = "")
public class HeadHunterImpl extends HeadHunter {
    @Override
    protected void expandValidHeadSearch(Properties properties, TwinClassEntity twinClassEntity, BasicSearch basicSearch) throws ServiceException {
    }

    @Override
    protected boolean isCreatableChildClass(Properties properties, TwinEntity twinEntity, TwinClassEntity twinClassEntity) throws ServiceException {
        return true;
    }
}
