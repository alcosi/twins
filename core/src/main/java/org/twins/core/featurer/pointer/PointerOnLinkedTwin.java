package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.TwinLinkService;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3103,
        name = "Some linked twin pointed (by link id)",
        description = "")
@RequiredArgsConstructor
public class PointerOnLinkedTwin extends Pointer {
    @FeaturerParam(name = "Link", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @Lazy
    private final TwinLinkService twinLinkService;

    @Override
    protected TwinEntity point(Properties properties, TwinEntity srcTwinEntity) throws ServiceException {
        twinLinkService.loadTwinLinks(srcTwinEntity);
        UUID linkIdValue = linkId.extract(properties);
        List<TwinLinkEntity> forwardLinks = srcTwinEntity.getTwinLinks().getForwardLinks().getGrouped(linkIdValue);
        if (CollectionUtils.isEmpty(forwardLinks))
            return null;
        else if (forwardLinks.size() > 1)
            throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE, srcTwinEntity.logShort() + " has " + forwardLinks.size() + " linked twins by link[" + linkIdValue + "]");
        else
            return forwardLinks.getFirst().getDstTwin();
    }
}
