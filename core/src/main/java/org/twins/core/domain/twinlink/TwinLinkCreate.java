package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Collection;
import java.util.List;

/**
 * Domain object for twin_link creation — mirrors the TwinClassSave/TwinClassCreate composition pattern:
 * wraps the {@link TwinLinkEntity} plus creation-only inputs that must not live on the entity itself.
 * Produced by TwinLinkAddRestDTOReverseMapper / TwinLinkAddTemporalRestDTOReverseMapper,
 * carried natively by {@link org.twins.core.domain.twinoperation.TwinCreate#getLinksCreateList()} and
 * {@link TwinLinkCUD#getCreateList()}, consumed by {@link org.twins.core.service.twinlink.TwinLinkService#addLinks}.
 */
@Data
@Accessors(chain = true)
public class TwinLinkCreate {
    private TwinLinkEntity twinLink;

    /**
     * Initial relation-attribute field values for the relation twin. Converted from
     * TwinLinkAddDTOv1.relationTwinFields (Map&lt;String,String&gt;) by TwinLinkAddRestDTOReverseMapper
     * via TwinFieldValueRestDTOReverseMapperV2.mapFields — same layer/pattern as TwinCreateRqRestDTOReverseMapper:47;
     * the link lookup is batched in beforeCollectionConversion via the MapperContext cache (no N+1).
     */
    private List<FieldValue> relationTwinFields;

    /** Wraps plain entities into composition objects (no relation twin fields). */
    public static List<TwinLinkCreate> wrapAll(Collection<TwinLinkEntity> twinLinks) {
        return twinLinks.stream().map(twinLink -> new TwinLinkCreate().setTwinLink(twinLink)).toList();
    }
}
