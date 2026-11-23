package org.twins.core.domain.twin;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValueSingle;

import java.util.List;

@Data
@Accessors(chain = true)
public class TwinGroup {
    /**
     * The key as a single field value: carries the key type (by its class, for the mapper) and the
     * loaded key entity (user, status, class, twin, data list option). Basic-field keys are born as
     * FieldValueReferenceSingle (id only) and bulk-materialized by TwinGroupedSearchService. The
     * empty key holds an undefined value of the level's type, so the type survives even without a key.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private FieldValueSingle keyValue;

    private List<TwinEntity> twins;
    private List<TwinGroup> groups;
    private int count;

    /**
     * The scalar key the tree was built by (the dto value and the sort token): an entity id for
     * user/status/class/twin/option keys, the scalar itself for TEXT/BOOLEAN/TIMESTAMP keys, null for
     * the empty key. Derived from keyValue — never stored, so the two can never disagree.
     */
    public Object getKey() {
        return keyValue == null ? null : keyValue.getScalarValue();
    }
}
