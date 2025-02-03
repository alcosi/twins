package org.twins.core.featurer.linker;

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
@Featurer(id = FeaturerTwins.ID_3001,
        name = "Impl",
        description = "")
public class LinkerImpl extends Linker {
    @Override
    protected void expandValidLinkedTwinSearch(Properties properties, TwinClassEntity twinClassEntity, TwinEntity headTwinEntity, BasicSearch basicSearch) throws ServiceException {

    }

    @Override
    public void expandValidLinkedTwinSearch(Properties properties, TwinEntity twinEntity, BasicSearch basicSearch) {

    }
}
