package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsLinkId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3108,
        name = "Head linked twin pointed",
        description = "src -> head -> linked twin (by link id, single)")
@RequiredArgsConstructor
public class PointerOnHeadLinkedTwin extends Pointer {
    @FeaturerParam(name = "Link", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @Lazy
    private final TwinLinkService twinLinkService;

    @Lazy
    private final TwinService twinService;

    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        // hop 1: src -> head
        Map<UUID, TwinEntity> heads = toHead(twinService, identity(srcTwins));
        // hop 2: head -> single-linked twin (by link id)
        return followSingleForwardLink(twinLinkService, heads, linkIdValue, optional);
    }
}
