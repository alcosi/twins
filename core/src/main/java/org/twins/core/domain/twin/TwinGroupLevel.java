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
    private TwinClassFieldEntity field;

    /**
     * The group keys of one twin at this level, read from the level field's current value
     * (TwinFieldService.getFieldValue — base and dynamic fields alike): a single value (the scalar
     * keys, the typed empty value = the empty group), or several single-key values for multi-value
     * fields (datalist, link) — then the twin lands in every branch.
     */
    public Collection<FieldValueSingle> extractKeys(FieldValue value) throws ServiceException {
        if (value instanceof FieldValueLink link) {
            // the key is the far twin; a backward link field has empty values
            // (backward loading is disabled platform-wide) and groups under the empty key
            Map<UUID, FieldValueSingle> keys = new LinkedHashMap<>();
            for (TwinEntity end : link.getItemsOrEmpty()) {
                if (end != null && end.getId() != null && !keys.containsKey(end.getId())) {
                    keys.put(end.getId(), new FieldValueLinkSingle(field).setValue(end));
                }
            }
            return keys.isEmpty() ? List.of(new FieldValueLinkSingle(field)) : List.copyOf(keys.values());
        }
        if (value instanceof FieldValueSelect select) {
            Map<UUID, FieldValueSingle> keys = new LinkedHashMap<>();
            for (DataListOptionEntity option : select.getItemsOrEmpty()) {
                if (option != null && option.getId() != null && !keys.containsKey(option.getId())) {
                    keys.put(option.getId(), new FieldValueSelectSingle(field).setValue(option));
                }
            }
            return keys.isEmpty() ? List.of(new FieldValueSelectSingle(field)) : List.copyOf(keys.values());
        }
        if (value instanceof FieldValueSingle scalar) {
            // simple values and dates; an undefined value is the empty key of its type (null scalar);
            // a decimal also lands here as FieldValueText: the typer formats it to a canonical
            // string ("0.00"), so the string itself is a stable group key
            return List.of(scalar);
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "Field " + field.getId() + " is not countable");
    }
}
