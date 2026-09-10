package org.twins.core.featurer.linker;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3003,
        name = "By head",
        description = "")
public class LinkerByHead extends Linker {

    @Override
    protected void expandValidLinkedTwinSearch(Properties properties, TwinClassEntity twinClassEntity, TwinEntity headTwinEntity, BasicSearch basicSearch) throws ServiceException {
        if (headTwinEntity == null)
            throw new ServiceException(ErrorCodeTwins.HEAD_TWIN_NOT_SPECIFIED, "headTwinId is required for this link (LinkerByHead)");
        basicSearch
                .addHeadTwinId(headTwinEntity.getId());
    }

    @Override
    public void expandValidLinkedTwinSearch(Properties properties, LinkEntity linkEntity, boolean forwardElseBackward, TwinEntity twinEntity, BasicSearch basicSearch, boolean throwOrEmpty) {
        basicSearch
                .addHeadTwinId(twinEntity.getHeadTwinId());
    }
}
