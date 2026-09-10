package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.service.link.LinkService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * One (twin, link, direction) pair of a state-based twin_link reconcile: the DESIRED far twins that the
 * twin must end up linked with via this link — a link-typed field serializes into exactly one such pair.
 * The direction is set EXPLICITLY by the caller (the field typer knows it — FieldTyperForwardLink /
 * FieldTyperBackwardLink): a link may be configured between the same twin class on both ends, where
 * class-based detection is ambiguous. Carried in a batch to
 * {@link org.twins.core.service.twinlink.TwinLinkService#reconcileLinks(java.util.Collection, org.twins.core.domain.TwinChangesCollector)}
 * so all pairs of a request share per-direction stored-links queries and one CUD pipeline run per twin.
 */
@Data
@RequiredArgsConstructor
public class TwinLinkReconcile {
    private final TwinEntity twin;
    private final LinkEntity link;
    private final LinkService.LinkDirection linkDirection;
    private final List<TwinEntity> toTwins;

    /**
     * Computed by TwinLinkService during reconcileLinks: the stored twin_links of (twin, link) keyed by the
     * far endpoint twin id. Internal processing state — not part of the input contract.
     */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<UUID, TwinLinkEntity> storedLinksMap;
}
