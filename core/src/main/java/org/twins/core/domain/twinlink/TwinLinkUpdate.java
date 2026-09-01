package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.List;

/**
 * Domain object for twin_link update — the update-path sibling of {@link TwinLinkCreate}:
 * wraps the {@link TwinLinkEntity} plus update-only inputs that must not live on the entity itself.
 * Produced by TwinLinkUpdateRestDTOReverseMapper, carried by {@link TwinLinkCUD#getUpdateList()},
 * consumed by {@link org.twins.core.service.twinlink.TwinLinkService#updateTwinLinks}.
 */
@Data
@Accessors(chain = true)
public class TwinLinkUpdate {
    private TwinLinkEntity twinLink;

    /**
     * New values for the relation twin's fields (relation attributes). Converted from
     * TwinLinkUpdateDTOv1.relationTwinFields (Map&lt;String,String&gt;) by TwinLinkUpdateRestDTOReverseMapper
     * via RelationTwinFieldsConverter — resolved against the relation twin itself (its id equals the
     * twin_link id by ID equality), with twin lookups batched in beforeCollectionConversion (no N+1).
     */
    private List<FieldValue> relationTwinFields;
}
