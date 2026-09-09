package org.twins.core.featurer.linker;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3003,
        name = "By head",
        description = "")
public class LinkerByHead extends Linker {

    @Override
    protected void expandValidLinkedTwinSearch(Properties properties, TwinClassEntity twinClassEntity, TwinEntity twinEntity, BasicSearch basicSearch) throws ServiceException {
        basicSearch
                .addHeadTwinId(twinEntity.getId());
    }

    @Override
    public void expandValidLinkedTwinSearch(Properties properties, LinkEntity linkEntity, boolean forwardElseBackward, TwinEntity twinEntity, BasicSearch basicSearch) {
        basicSearch
                .addHeadTwinId(twinEntity.getId());
    }
}
