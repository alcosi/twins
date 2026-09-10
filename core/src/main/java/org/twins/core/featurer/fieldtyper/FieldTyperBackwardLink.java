package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.enums.link.LinkType;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;

/**
 * Link-typed field viewed from the link's dst end: the value carries the SRC (far) twins. OneToOne-ONLY by
 * design: uniqForDstTwin bounds a twin's backward links to at most one, so the state-based diff over the
 * backward side stays bounded — many-typed backward links are unbounded (hundreds+ of sources may point at
 * one twin) and are rejected, both here and in TwinLinkService.reconcileLinks. See {@link FieldTyperLinkBase}
 * for the shared field mechanics.
 */
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_1359,
        name = "Linked twin backward",
        description = "")
public class FieldTyperBackwardLink extends FieldTyperLinkBase {
    public static final Integer ID = 1359;

    @Override
    protected LinkService.LinkDirection linkDirection() {
        return LinkService.LinkDirection.backward;
    }

    @Override
    protected boolean isSupportedLinkType(LinkEntity linkEntity) {
        return LinkType.OneToOne.equals(linkEntity.getType());
    }
}
