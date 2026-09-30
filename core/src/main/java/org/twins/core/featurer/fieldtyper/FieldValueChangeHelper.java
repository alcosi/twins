package org.twins.core.featurer.fieldtyper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.user.UserEntity;
import org.twins.core.featurer.fieldtyper.value.*;

import java.util.*;
import java.util.function.Function;

public class FieldValueChangeHelper {
    public static boolean isSingleToSingleValueUpdate(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && oldValueMap != null && oldValueMap.size() == 1;
    }

    public static boolean isAnyToSingleValueUpdate(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && oldValueMap != null && oldValueMap.size() >= 1;
    }

    public static boolean isSingleValueAdd(List<?> newValueList, Map<UUID, ?> oldValueMap) {
        return newValueList != null && newValueList.size() == 1 && MapUtils.isEmpty(oldValueMap);
    }

    public static boolean notSaved(UUID valueId, Map<UUID, ?> oldValueMap) {
        return oldValueMap == null || !oldValueMap.containsKey(valueId);
    }

    public static boolean hasOutOfDateValues(Map<UUID, ?> oldValueMap) {
        return oldValueMap != null && CollectionUtils.isNotEmpty(oldValueMap.entrySet());
    }

    /**
     * Same items (matched by the extracted id) with the same multiplicity.
     * A null item or a null id is not comparable and yields false.
     */
    public static <E> boolean sameIdMultiset(Collection<E> left, Collection<E> right, Function<E, UUID> getId) {
        if (left == null || right == null || left.size() != right.size())
            return false;
        Map<UUID, Integer> counts = new HashMap<>();
        for (E item : left) {
            UUID id = item != null ? getId.apply(item) : null;
            if (id == null)
                return false;
            counts.merge(id, 1, Integer::sum);
        }
        for (E item : right) {
            UUID id = item != null ? getId.apply(item) : null;
            Integer count = id != null ? counts.get(id) : null;
            if (count == null)
                return false;
            if (count == 1)
                counts.remove(id);
            else
                counts.put(id, count - 1);
        }
        return counts.isEmpty();
    }

    /**
     * Every incoming item id is already stored. Extra stored ids are ignored.
     * A null item or a null id is not comparable and yields false.
     */
    public static <E> boolean containsAllIds(Collection<E> stored, Collection<E> incoming, Function<E, UUID> getId) {
        if (stored == null || incoming == null)
            return false;
        Set<UUID> storedIds = new HashSet<>();
        for (E item : stored) {
            UUID id = item != null ? getId.apply(item) : null;
            if (id == null)
                return false;
            storedIds.add(id);
        }
        for (E item : incoming) {
            UUID id = item != null ? getId.apply(item) : null;
            if (id == null || !storedIds.contains(id))
                return false;
        }
        return true;
    }

    /**
     * Content comparison of the stored and the incoming value of the same field.
     * Errs towards "not same": an unknown value type, an undefined/cleared side or a null item
     * never reads as equal, so the caller keeps the value permission-gated.
     */
    public static boolean sameContents(FieldValue stored, FieldValue incoming) {
        if (stored == null || incoming == null || stored.getClass() != incoming.getClass()
                || stored.isUndefined() || stored.isCleared()
                || incoming.isUndefined() || incoming.isCleared())
            return false;
        if (stored instanceof FieldValueSimple<?> simple)
            return Objects.equals(simple.getValue(), ((FieldValueSimple<?>) incoming).getValue());
        if (stored instanceof FieldValueDate date)
            return Objects.equals(date.getDate(), ((FieldValueDate) incoming).getDate());
        if (stored instanceof FieldValueStatus)
            return sameItemId(((FieldValueStatus) stored).getValue(), ((FieldValueStatus) incoming).getValue(), TwinStatusEntity::getId);
        if (stored instanceof FieldValueLinkSingle)
            return sameItemId(((FieldValueLinkSingle) stored).getValue(), ((FieldValueLinkSingle) incoming).getValue(), TwinEntity::getId);
        if (stored instanceof FieldValueTwinClassSingle)
            return sameItemId(((FieldValueTwinClassSingle) stored).getValue(), ((FieldValueTwinClassSingle) incoming).getValue(), TwinClassEntity::getId);
        if (stored instanceof FieldValueUserSingle)
            return sameItemId(((FieldValueUserSingle) stored).getValue(), ((FieldValueUserSingle) incoming).getValue(), UserEntity::getId);
        if (stored instanceof FieldValueSelect select)
            return sameIdMultiset(select.getItemsOrEmpty(), ((FieldValueSelect) incoming).getItemsOrEmpty(), DataListOptionEntity::getId);
        if (stored instanceof FieldValueLink link)
            return sameIdMultiset(link.getItemsOrEmpty(), ((FieldValueLink) incoming).getItemsOrEmpty(), TwinEntity::getId);
        if (stored instanceof FieldValueUser user)
            return sameIdMultiset(user.getItems(), ((FieldValueUser) incoming).getItems(), UserEntity::getId);
        if (stored instanceof FieldValueTwinClassList twinClasses)
            return sameIdMultiset(twinClasses.getItems(), ((FieldValueTwinClassList) incoming).getItems(), TwinClassEntity::getId);
        if (stored instanceof FieldValueAliases)
            return sameAliasKeys((FieldValueAliases) stored, (FieldValueAliases) incoming);
        return false;
    }

    private static <E> boolean sameItemId(E stored, E incoming, Function<E, UUID> getId) {
        return stored != null && incoming != null && Objects.equals(getId.apply(stored), getId.apply(incoming));
    }

    /**
     * Aliases are compared by (type, value) pairs: the client resends alias strings, not row ids.
     */
    private static boolean sameAliasKeys(FieldValueAliases stored, FieldValueAliases incoming) {
        if (stored.getItems().size() != incoming.getItems().size())
            return false;
        Map<String, Integer> counts = new HashMap<>();
        for (TwinAliasEntity alias : stored.getItems()) {
            if (alias == null)
                return false;
            counts.merge(aliasKey(alias), 1, Integer::sum);
        }
        for (TwinAliasEntity alias : incoming.getItems()) {
            if (alias == null)
                return false;
            Integer count = counts.get(aliasKey(alias));
            if (count == null)
                return false;
            if (count == 1)
                counts.remove(aliasKey(alias));
            else
                counts.put(aliasKey(alias), count - 1);
        }
        return counts.isEmpty();
    }

    private static String aliasKey(TwinAliasEntity alias) {
        return alias.getAliasTypeId() + "|" + alias.getAlias();
    }
}
