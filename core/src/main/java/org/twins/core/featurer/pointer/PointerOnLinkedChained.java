package org.twins.core.featurer.pointer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUIDList;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.enums.consts.SystemIds;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDListTwinsLinkId;
import org.twins.core.service.link.TwinLinkService;
import org.twins.core.service.twin.TwinService;

import java.util.*;

/**
 * Universal link/head chain pointer. Follows an ordered list of navigation tokens from the source
 * twin: a normal token is a {@code linkId} (follow the single forward link of that type), and the
 * reserved system pseudo-field {@link SystemIds.TwinClassField.Base#HEAD_ID} means a head hop.
 *
 * <p>This single pointer covers deep link chains, mixed link/head navigation and head depth:
 * {@code [l1, l2]} = link -> link; {@code [linkId, HEAD_ID]} = linked twin -> head (same as
 * {@link PointerOnLinkedTwinHead}); {@code [HEAD_ID, linkId]} = head -> linked twin (same as
 * {@link PointerOnHeadLinkedTwin}); {@code [HEAD_ID, HEAD_ID, ...]} = deep head.
 */
@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_3109,
        name = "Linked chain pointed",
        description = "ordered chain of link ids; the HEAD_ID system field token means a head hop")
@RequiredArgsConstructor
public class PointerOnLinkedChained extends Pointer {
    public static final int MAX_DEPTH = 10;

    @FeaturerParam(name = "Link ids",
            description = "ordered comma-separated link ids; the HEAD_ID system field token means a head hop",
            order = 1)
    public static final FeaturerParamUUIDList linkIds = new FeaturerParamUUIDListTwinsLinkId("linkIds");

    @Lazy
    private final TwinLinkService twinLinkService;

    @Lazy
    private final TwinService twinService;

    @Override
    protected Map<UUID, TwinEntity> load(Properties properties, Collection<TwinEntity> srcTwins, boolean optional) throws ServiceException {
        List<UUID> links = linkIds.extract(properties);
        if (links.isEmpty()) {
            return Map.of();
        }
        if (links.size() > MAX_DEPTH) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "PointerOnLinkedChained chain too deep: " + links.size() + " > " + MAX_DEPTH);
        }
        Map<UUID, TwinEntity> iterator = identity(srcTwins);
        for (var link : links) {
            if (iterator.isEmpty()) {
                break;
            }
            iterator = SystemIds.TwinClassField.Base.HEAD_ID.equals(link)
                    ? toHead(twinService, iterator)
                    : followSingleForwardLink(twinLinkService, iterator, link, optional);
        }
        return iterator;
    }
}
