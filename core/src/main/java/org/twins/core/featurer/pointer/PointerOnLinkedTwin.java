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

import java.util.*;

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
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        twinLinkService.loadTwinLinks(srcTwins);
        UUID linkIdValue = linkId.extract(properties);
        Map<UUID, TwinEntity> result = new HashMap<>(srcTwins.size());
        var links = new ArrayList<TwinLinkEntity>(srcTwins.size());
        for (var twin : srcTwins) {
            List<TwinLinkEntity> forwardLinks = twin.getTwinLinks().getForwardLinks().getGrouped(linkIdValue);
            if (CollectionUtils.isEmpty(forwardLinks)) {
                continue;
            }
            if (forwardLinks.size() > 1) {
                if (optional) {
                    log.warn("Optional pointer: {} has {} linked twins by link[{}]; skipping this twin", twin.logShort(), forwardLinks.size(), linkIdValue);
                    continue;
                }
                throw new ServiceException(ErrorCodeTwins.POINTER_NON_SINGLE,
                        twin.logShort() + " has " + forwardLinks.size() + " linked twins by link[" + linkIdValue + "]");
            }
            TwinLinkEntity link = forwardLinks.getFirst();
            links.add(link);
        }
        if (links.isEmpty()) {
            return Map.of();
        }
        twinLinkService.loadDstTwin(links);
        for (var link : links) {
            result.put(link.getSrcTwinId(), link.getDstTwin());
        }
        return result;
    }
}
