package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.link.LinkService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Declarative intent to link a twin (the near side) to far twins via a link in an explicit direction:
 * "link TWIN --LINK[direction]--> these far twins". Producers never build half-assembled twin_link rows —
 * {@link org.twins.core.service.twinlink.TwinLinkService#prepareTwinLinks} builds the ready rows from the
 * intent (one per far twin) and absorbs the former relink/dedup step. The direction is MANDATORY: a link
 * configured between the same twin class on both ends is undetectable by classes — the caller (field typer,
 * links[] mapper, factory filler) states the intent explicitly. Carried by
 * {@link org.twins.core.domain.twinoperation.TwinCreate#getLinksCreateList()} and
 * {@link TwinLinkCUD#getCreateList()}, consumed by
 * {@link org.twins.core.service.twinlink.TwinLinkService#addLinks} (operation-based) and
 * {@link org.twins.core.service.twinlink.TwinLinkService#reconcileLinks} (state-based).
 */
@Data
@Accessors(chain = true)
public class TwinLinkCreate {
    /** The near side of every built row: src for forward, dst for backward. Stamped by addLinks when null. */
    private TwinEntity twin;
    private LinkEntity link;
    /** Mandatory intent direction — forward: src=twin/dst=far; backward: src=far/dst=twin. */
    private LinkService.LinkDirection linkDirection;
    /** The far sides; id-stubs are fine — prepare loads the real twins (needed for class validation). */
    private List<TwinEntity> toTwins;
    /** Initial relation-attribute values for this pair's relation twins (links[] API; empty for field writes). */
    private List<FieldValue> relationTwinFields;
    /** Caller policy (factory fillers): for uniqForSrcTwin links, adopt an existing link instead of duplicating. */
    private boolean uniqForSrcRelink;

    /** Built by prepareTwinLinks: the ready rows (one per toTwin; deduped/relink-adopted). */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<TwinLinkEntity> twinLinks;

    /** Reconcile processing state: stored twin_links of (twin, link) keyed by the far endpoint twin id. */
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Map<UUID, TwinLinkEntity> storedLinksMap;

    public List<TwinLinkEntity> getTwinLinksSafe() {
        if (twinLinks == null)
            twinLinks = new ArrayList<>();
        return twinLinks;
    }

    /** Convenience for single-link intents (the links[] API maps one DTO item to one far twin). */
    public TwinLinkCreate addToTwin(TwinEntity toTwin) {
        if (toTwins == null)
            toTwins = new ArrayList<>();
        toTwins.add(toTwin);
        return this;
    }
}
