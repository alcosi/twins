package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twin.TwinGroupKeyType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

@Data
@Accessors(chain = true)
public class TwinGroupLevel {
    private UUID fieldId;
    private TwinGroupSort sort;
    private TwinGroupKeyType keyType;
    private TwinEntity.BasicField basicField;
    private TwinClassFieldEntity dynamicField;

    /**
     * The keys of one twin at this level: a single key (null = the empty group), or several for
     * multi-value fields (datalist, link) — then the twin lands in every branch. Valid only after
     * the level was resolved (basicField or dynamicField are set) and TwinService.loadFieldsValues
     * filled twin.fieldValuesKit.
     */
    public List<Object> extractKeys(TwinEntity twin) throws ServiceException {
        if (basicField != null) {
            return Collections.singletonList(basicField.getValue(twin));
        }
        FieldValue value = twin.getFieldValuesKit() == null ? null : twin.getFieldValuesKit().get(dynamicField.getId());
        if (value == null || value.isEmpty()) {
            // a twin of a class without the field has no value and groups under the empty key
            return Collections.singletonList(null);
        }
        if (value instanceof FieldValueLink link) {
            // the key is the id of a far twin; a backward link field has empty values
            // (backward loading is disabled platform-wide) and groups under the empty key
            return itemIds(link.getItemsOrEmpty(), TwinEntity::getId);
        }
        if (value instanceof FieldValueSelect select) {
            return itemIds(select.getItemsOrEmpty(), DataListOptionEntity::getId);
        }
        if (value instanceof FieldValueDate date) {
            return Collections.singletonList(date.getDate());
        }
        if (value instanceof FieldValueSimple<?> simple) {
            Object scalar = simple.getValue();
            if (keyType == TwinGroupKeyType.DECIMAL && scalar != null) {
                // a decimal deserializes to its formatted text ("0.00"); parse it back to a scale-independent key
                scalar = new BigDecimal(scalar.toString()).stripTrailingZeros();
            }
            return Collections.singletonList(scalar);
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "Field " + dynamicField.getId() + " is not countable");
    }

    private static <T> List<Object> itemIds(List<T> items, Function<T, UUID> id) {
        Set<Object> ids = new LinkedHashSet<>();
        for (T item : items) {
            UUID itemId = item == null ? null : id.apply(item);
            if (itemId != null) {
                ids.add(itemId);
            }
        }
        return ids.isEmpty() ? Collections.singletonList(null) : new ArrayList<>(ids);
    }
}
