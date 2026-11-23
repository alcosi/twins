package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;

import java.util.*;

@Data
@Accessors(chain = true)
public class TwinGroupLevel {
    private UUID fieldId;
    private TwinGroupSort sort;
    private TwinEntity.BasicField basicField;
    private TwinClassFieldEntity field;
    private FieldValue keyPrototype;

    /**
     * The group keys of one twin at this level: a single undefined value (the empty group), or several
     * single-key values for multi-value fields (datalist, link) — then the twin lands in every branch.
     * Valid only after the level was resolved (basicField or field + keyPrototype are set) and
     * TwinService.loadFieldsValues filled twin.fieldValuesKit. The scalar key of each returned value is
     * its getScalarValue().
     */
    public Collection<FieldValueSingle> extractKeys(TwinEntity twin) throws ServiceException {
        if (basicField != null) {
            UUID id = (UUID) basicField.getValue(twin);
            // base fields are not in twin.fieldValuesKit: the key starts as an id-only reference
            // and is bulk-materialized by the service into a concrete value
            return id == null ? emptyKey()
                    : List.of(new FieldValueReferenceSingle(field, keyPrototype.getClass(), id));
        }
        FieldValue value = twin.getFieldValuesKit() == null ? null : twin.getFieldValuesKit().get(field.getId());
        if (value == null || value.isEmpty()) {
            // a twin of a class without the field has no value and groups under the empty key
            return emptyKey();
        }
        if (value instanceof FieldValueLink link) {
            // the key is the far twin; a backward link field has empty values
            // (backward loading is disabled platform-wide) and groups under the empty key
            Map<UUID, FieldValueSingle> keys = new LinkedHashMap<>();
            for (TwinEntity end : link.getItemsOrEmpty()) {
                if (end != null && end.getId() != null && !keys.containsKey(end.getId())) {
                    keys.put(end.getId(), new FieldValueLinkSingle(field).setValue(end));
                }
            }
            return keys.isEmpty() ? emptyKey() : keys.values();
        }
        if (value instanceof FieldValueSelect select) {
            Map<UUID, FieldValueSingle> keys = new LinkedHashMap<>();
            for (DataListOptionEntity option : select.getItemsOrEmpty()) {
                if (option != null && option.getId() != null && !keys.containsKey(option.getId())) {
                    keys.put(option.getId(), new FieldValueSelectSingle(field).setValue(option));
                }
            }
            return keys.isEmpty() ? emptyKey() : keys.values();
        }
        if (value instanceof FieldValueSingle scalar) {
            // simple values and dates; a decimal also lands here as FieldValueText: the typer formats
            // it to a canonical string ("0.00"), so the string itself is a stable group key
            return List.of(scalar);
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "Field " + field.getId() + " is not countable");
    }

    private Collection<FieldValueSingle> emptyKey() {
        // an undefined value of the level's type, so the key type survives even without a key;
        // resolve* sets only single-valued prototypes, so the cast cannot fail
        return List.of((FieldValueSingle) keyPrototype.newInstance(field));
    }
}
