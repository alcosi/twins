package org.twins.core.domain.twinlink;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinLinkEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.List;

/**
 * Base composition object for twin_link save operations — mirrors the TwinClassSave/TwinClassCreate pattern:
 * wraps the {@link TwinLinkEntity} plus inputs that must not live on the entity itself. Shared by the
 * create path ({@link TwinLinkCreate}) and the update path ({@link TwinLinkUpdate}).
 */
@Data
@Accessors(chain = true)
public class TwinLinkSave {
    private TwinLinkEntity twinLink;

    /**
     * Relation-attribute field values for the relation twin: initial values on the create path, new values
     * on the update path. Converted from the DTO relationTwinFields (Map&lt;String,String&gt;) at the reverse-mapper
     * layer via RelationTwinFieldsConverter — same layer/pattern as TwinCreateRqRestDTOReverseMapper:47;
     * lookups are batched in beforeCollectionConversion via the MapperContext cache (no N+1).
     */
    private List<FieldValue> relationTwinFields;
}
