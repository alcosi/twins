package org.twins.core.featurer.linker;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3004,
        name = "Exclude conflicted",
        description = "Excludes twins that are already bound to the incoming twin by the conflicted link (filters the valid-twins search; in twin_link validation an empty match rejects the link)")
public class LinkerExcludeConflicted extends Linker {

    @FeaturerParam(name = "Conflicted link id", description = "Reverse link id to exclude on the related twin", order = 1)
    public static final FeaturerParamUUID conflictedLinkId = new FeaturerParamUUIDTwinsLinkId("conflictedLinkId");

    @Lazy
    @Autowired
    TwinLinkService twinLinkService;

    @Override
    protected void expandValidLinkedTwinSearch(Properties properties, TwinClassEntity twinClassEntity, TwinEntity twinEntity, BasicSearch basicSearch) throws ServiceException {

    }

    @Override
    public void expandValidLinkedTwinSearch(Properties properties, LinkEntity linkEntity, boolean forwardElseBackward, TwinEntity twinEntity, BasicSearch basicSearch, boolean throwOrEmpty) throws ServiceException {
        UUID conflictedId = conflictedLinkId.extract(properties);

        boolean hasConflictedLink = forwardElseBackward
                ? twinLinkService.existsSrcTwinIdsByLinkId(twinEntity.getId(), conflictedId)
                : twinLinkService.existsDstTwinIdsByLinkId(twinEntity.getId(), conflictedId);

        if (hasConflictedLink) {
            // conflicted: no twin is allowed — short-circuit the search into a guaranteed empty result
            basicSearch.setEmptyResult(true);
        }
    }
}
