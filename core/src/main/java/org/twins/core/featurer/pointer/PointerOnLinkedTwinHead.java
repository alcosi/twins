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
@Featurer(id = FeaturerTwins.ID_3107,
        name = "Linked twin head pointed",
        description = "src -> linked twin (by link id, single) -> its head")
@RequiredArgsConstructor
public class PointerOnLinkedTwinHead extends Pointer {
    @FeaturerParam(name = "Link", description = "", order = 1)
    public static final FeaturerParamUUID linkId = new FeaturerParamUUIDTwinsLinkId("linkId");

    @Lazy
    private final TwinLinkService twinLinkService;

    @Lazy
    private final TwinService twinService;

    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins) throws ServiceException {
        UUID linkIdValue = linkId.extract(properties);
        // hop 1: src -> single-linked twin (by link id)
        Map<UUID, TwinEntity> linked = followSingleForwardLink(twinLinkService, identity(srcTwins), linkIdValue);
        // hop 2: linked twin -> its head
        return toHead(twinService, linked);
    }
}
