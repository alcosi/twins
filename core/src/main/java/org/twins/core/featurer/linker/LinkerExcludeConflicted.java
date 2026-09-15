package org.twins.core.featurer.linker;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkRepository;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3004,
        name = "Exclude conflicted",
        description = "Excludes twins already bound by the conflicted link (filtered from the valid-twins search; any such link rejects the twin_link)")
public class LinkerExcludeConflicted extends Linker {

    @FeaturerParam(name = "Conflicted link id", description = "Reverse link id to exclude on the related twin", order = 1)
    public static final FeaturerParamUUID conflictedLinkId = new FeaturerParamUUIDTwinsLinkId("conflictedLinkId");

    @Lazy
    @Autowired
    private TwinLinkRepository twinLinkRepository;

    @Override
    protected void expandValidLinkedTwinSearch(Properties properties, TwinClassEntity twinClassEntity, TwinEntity twinEntity, BasicSearch basicSearch) throws ServiceException {

    }

    @Override
    public void expandValidLinkedTwinSearch(Properties properties, TwinEntity twinEntity, boolean forwardElseBackward, BasicSearch basicSearch, boolean searchElseValidate) {
        UUID extractedConflictedLinkId = conflictedLinkId.extract(properties);

        List<UUID> conflictedTwinLinkIds;
        if (forwardElseBackward) {
            conflictedTwinLinkIds = twinLinkRepository.findDstTwinIdsBySrcTwinIdAndLinkId(twinEntity.getId(), extractedConflictedLinkId);
        } else {
            conflictedTwinLinkIds = twinLinkRepository.findSrcTwinIdsByDstTwinIdAndLinkId(twinEntity.getId(), extractedConflictedLinkId);
        }

        if (conflictedTwinLinkIds.isEmpty())
            return;
        if (searchElseValidate) {
            basicSearch.addTwinId(conflictedTwinLinkIds, true);
        } else {
            basicSearch.setEmptyResult(true);
        }

    }
}
