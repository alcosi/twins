package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.storage.*;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLink;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.function.Function;

public final class TwinGroupKeyValues {
    private TwinGroupKeyValues() {
    }

    public static List<Object> basic(TwinEntity twin, TwinEntity.BasicField field) {
        return Collections.singletonList(field.getValue(twin));
    }

    public static List<Object> dynamic(TwinEntity twin, TwinClassFieldEntity field, TwinFieldStorage storage) throws ServiceException {
        UUID fieldId = field.getId();
        if (storage instanceof TwinFieldStorageDatalist) {
            return dataList(twin.getTwinFieldDatalistKit(), fieldId);
        }
        if (storage instanceof TwinFieldStorageSimple) {
            return single(twin.getTwinFieldSimpleKit(), fieldId, TwinFieldSimpleEntity::getValue);
        }
        if (storage instanceof TwinFieldStorageDecimal) {
            return single(twin.getTwinFieldDecimalKit(), fieldId, row -> {
                BigDecimal value = row.getValue();
                return value == null ? null : value.stripTrailingZeros();
            });
        }
        if (storage instanceof TwinFieldStorageBoolean) {
            return single(twin.getTwinFieldBooleanKit(), fieldId, TwinFieldBooleanEntity::getValue);
        }
        if (storage instanceof TwinFieldStorageTimestamp) {
            return single(twin.getTwinFieldTimestampKit(), fieldId, row -> {
                Timestamp value = row.getValue();
                // LocalDateTime serializes without the JVM TZ shift a raw java.sql.Timestamp would add
                return value == null ? null : value.toLocalDateTime();
            });
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "Field " + fieldId + " is not countable");
    }

    /**
     * Keys of a link field: the ids of the far twins carried by its FieldValueLink from
     * twin.fieldValuesKit (filled by TwinService.loadFieldsValues).
     */
    public static List<Object> linkEnds(FieldValue fieldValue) {
        if (!(fieldValue instanceof FieldValueLink link) || !link.isNotEmpty()) {
            return Collections.singletonList(null);
        }
        Set<Object> ids = new LinkedHashSet<>();
        for (TwinEntity end : link.getItems()) {
            if (end != null && end.getId() != null) {
                ids.add(end.getId());
            }
        }
        if (ids.isEmpty()) {
            return Collections.singletonList(null);
        }
        return new ArrayList<>(ids);
    }

    private static List<Object> dataList(KitGrouped<TwinFieldDataListEntity, UUID, UUID> kit, UUID fieldId) {
        if (kit == null || !kit.containsGroupedKey(fieldId)) {
            return Collections.singletonList(null);
        }
        Set<Object> options = new LinkedHashSet<>();
        for (TwinFieldDataListEntity row : kit.getGrouped(fieldId)) {
            options.add(row.getDataListOptionId());
        }
        if (options.isEmpty()) {
            return Collections.singletonList(null);
        }
        return new ArrayList<>(options);
    }

    private static <E> List<Object> single(Kit<E, UUID> kit, UUID fieldId, Function<E, Object> value) {
        if (kit == null) {
            return Collections.singletonList(null);
        }
        E row = kit.get(fieldId);
        if (row == null) {
            return Collections.singletonList(null);
        }
        return Collections.singletonList(value.apply(row));
    }
}
