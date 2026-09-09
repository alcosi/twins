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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.twinlink.TwinLinkService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3004,
        name = "Exclude conflicted",
        description = "Resolves related twins by the incoming twin's forward or backward links, then excludes twins that have the conflicted link to any of those related twins")
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
    public void expandValidLinkedTwinSearch(Properties properties, LinkEntity linkEntity, boolean forwardElseBackward, TwinEntity twinEntity, BasicSearch basicSearch) throws ServiceException {
        UUID conflictedId = conflictedLinkId.extract(properties);
        UUID currentLinkId = linkEntity.getId();

        boolean hasConflictedLink = forwardElseBackward
                ? twinLinkService.existsSrcTwinIdsByLinkId(twinEntity.getId(), conflictedId)
                : twinLinkService.existsDstTwinIdsByLinkId(twinEntity.getId(), conflictedId);
        if (hasConflictedLink)
            throw new ServiceException(ErrorCodeTwins.TWIN_LINK_CONFLICTED, "current link[" + currentLinkId + "] can't be created, because has conflicted linkId[" + conflictedId + "]");

        if (forwardElseBackward)
            basicSearch.addLinkSrcTwinsId(currentLinkId, List.of(twinEntity.getId()), false, true);
        else
            basicSearch.addLinkDstTwinsId(currentLinkId, List.of(twinEntity.getId()), false, true);
    }
}
