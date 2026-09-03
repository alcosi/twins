package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * Domain object for twin_link update — the update-path sibling of {@link TwinLinkCreate}, both carrying
 * the shared {@link TwinLinkSave} fields. Produced by TwinLinkUpdateRestDTOReverseMapper, carried by
 * {@link TwinLinkCUD#getUpdateList()}, consumed by {@link org.twins.core.service.twinlink.TwinLinkService#updateTwinLinks}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinLinkUpdate extends TwinLinkSave {
}
