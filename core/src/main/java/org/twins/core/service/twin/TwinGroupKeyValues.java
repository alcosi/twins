package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldBooleanEntity;
import org.twins.core.dao.twin.TwinFieldDataListEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageBoolean;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDatalist;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTimestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
            return single(twin.getTwinFieldDecimalKit(), fieldId, TwinFieldDecimalEntity::getValue);
        }
        if (storage instanceof TwinFieldStorageBoolean) {
            return single(twin.getTwinFieldBooleanKit(), fieldId, TwinFieldBooleanEntity::getValue);
        }
        if (storage instanceof TwinFieldStorageTimestamp) {
            return single(twin.getTwinFieldTimestampKit(), fieldId, TwinFieldTimestampEntity::getValue);
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT,
                "Field " + fieldId + " is not countable");
    }

    public static boolean isDataList(TwinFieldStorage storage) {
        return storage instanceof TwinFieldStorageDatalist;
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
