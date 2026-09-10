package org.twins.core.featurer.fieldtyper;

import org.cambium.featurer.annotations.Featurer;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.link.LinkService;

/**
 * Link-typed field viewed from the link's src end: the twin OWNS its outgoing links, so any link type is
 * safe here (the stored side is bounded by the twin's own writes). See {@link FieldTyperLinkBase} for the
 * shared field mechanics.
 */
@Lazy
@Component
@Featurer(id = FeaturerTwins.ID_1310,
        name = "Linked twin",
        description = "")
public class FieldTyperForwardLink extends FieldTyperLinkBase {
    public static final Integer ID = 1310;

    @Override
    protected LinkService.LinkDirection linkDirection() {
        return LinkService.LinkDirection.forward;
    }
}
