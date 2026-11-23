package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.featurer.fieldtyper.value.*;

import java.util.*;

public final class TwinTreeAssembler {
    private TwinTreeAssembler() {
    }

    /** The current value of a level field on a twin — TwinFieldService.getFieldValue. */
    @FunctionalInterface
    public interface FieldValueGetter {
        FieldValue getFieldValue(TwinEntity twin, TwinClassFieldEntity field) throws ServiceException;
    }

    /**
     * Walks every twin down the levels, finding or creating a node per key, and places the twin
     * into the leaf node. A level with several keys (datalist, link) puts the twin into every
     * matching branch.
     */
    public static Assembly assemble(List<TwinEntity> twins, List<TwinGroupLevel> levels, FieldValueGetter valueGetter) throws ServiceException {
        Bucket root = new Bucket();
        Set<UUID> seen = new HashSet<>();
        for (TwinEntity twin : twins) {
            if (twin == null || twin.getId() == null || !seen.add(twin.getId())) {
                continue;
            }
            place(root, twin, levels, 0, valueGetter);
        }
        List<TwinGroup> all = new ArrayList<>();
        List<TwinGroup> roots = toGroups(root.children.values(), 0, levels, all);
        return new Assembly(roots, all);
    }

    private static void place(Bucket node, TwinEntity twin, List<TwinGroupLevel> levels, int levelIndex, FieldValueGetter valueGetter) throws ServiceException {
        // a twin visits each node at most once (the input is distinct, and the keys of a level are
        // distinct), so the visit itself is the count increment
        node.count++;
        if (levelIndex == levels.size()) {
            node.twins.add(twin);
            return;
        }
        TwinGroupLevel level = levels.get(levelIndex);
        for (FieldValueSingle keyValue : level.extractKeys(valueGetter.getFieldValue(twin, level.getField()))) {
            Bucket child = node.children.computeIfAbsent(keyValue.getScalarValue(), key -> new Bucket());
            if (child.keyValue == null) {
                child.keyValue = keyValue;
            }
            place(child, twin, levels, levelIndex + 1, valueGetter);
        }
    }

    private static List<TwinGroup> toGroups(Iterable<Bucket> buckets, int levelIndex, List<TwinGroupLevel> levels, List<TwinGroup> all) {
        boolean last = levelIndex == levels.size() - 1;
        List<TwinGroup> groups = new ArrayList<>();
        for (Bucket bucket : buckets) {
            TwinGroup group = new TwinGroup()
                    .setKeyValue(bucket.keyValue)
                    .setCount(bucket.count);
            all.add(group);
            if (last) {
                group.setTwins(bucket.twins);
            } else {
                group.setGroups(toGroups(bucket.children.values(), levelIndex + 1, levels, all));
            }
            groups.add(group);
        }
        return groups;
    }

    /**
     * Sorts every sibling list by its level's sort. VALUE sorts compare the display names of the key
     * entities, so the entities must be attached to the key values and the localized names of the
     * i18n-backed keys ({@link #localizedKeyIds}) bulk-translated before this call; the empty key is
     * always last.
     */
    public static void sort(List<TwinGroup> roots, List<TwinGroupLevel> levels, Map<UUID, String> localizedNames) {
        sort(roots, levels, localizedNames, 0);
    }

    private static void sort(List<TwinGroup> groups, List<TwinGroupLevel> levels, Map<UUID, String> localizedNames, int levelIndex) {
        if (groups == null || groups.isEmpty() || levelIndex >= levels.size()) {
            return;
        }
        groups.sort(order(levels.get(levelIndex), localizedNames));
        for (TwinGroup group : groups) {
            sort(group.getGroups(), levels, localizedNames, levelIndex + 1);
        }
    }

    private static Comparator<TwinGroup> order(TwinGroupLevel level, Map<UUID, String> localizedNames) {
        return switch (level.getSort()) {
            case VALUE_ASC -> values(true, localizedNames);
            case VALUE_DESC -> values(false, localizedNames);
            case COUNT_ASC -> counts(true);
            case COUNT_DESC -> counts(false);
        };
    }

    /**
     * The roots in grouping order, plus every group of every level as a flat list (for batch key
     * entity loading without a second tree walk).
     */
    public record Assembly(List<TwinGroup> roots, List<TwinGroup> all) {
    }

    public static Comparator<TwinGroup> keys(boolean ascending) {
        return (left, right) -> compareRaw(left.getKey(), right.getKey(), ascending);
    }

    public static Comparator<TwinGroup> counts(boolean ascending) {
        return (left, right) -> {
            int byCount = Integer.compare(left.getCount(), right.getCount());
            if (!ascending) {
                byCount = -byCount;
            }
            if (byCount != 0) {
                return byCount;
            }
            return compareRaw(left.getKey(), right.getKey(), true);
        };
    }

    /**
     * The i18n ids behind the localized key names of every group (statuses, classes and datalist
     * options have no plain name column) — the caller bulk-translates them once before sorting.
     * Users and twins carry a plain name column, scalar keys the value itself, so they contribute
     * nothing.
     */
    public static Set<UUID> localizedKeyIds(List<TwinGroup> groups) {
        Set<UUID> i18nIds = new LinkedHashSet<>();
        for (TwinGroup group : groups) {
            UUID i18nId = keyI18nId(group);
            if (i18nId != null) {
                i18nIds.add(i18nId);
            }
        }
        return i18nIds;
    }

    private static UUID keyI18nId(TwinGroup group) {
        if (group.getKeyValue() instanceof FieldValueStatus status && status.getValue() != null) {
            return status.getValue().getNameI18nId();
        }
        if (group.getKeyValue() instanceof FieldValueTwinClassSingle twinClass && twinClass.getValue() != null) {
            return twinClass.getValue().getNameI18NId();
        }
        if (group.getKeyValue() instanceof FieldValueSelectSingle option && option.getValue() != null) {
            return option.getValue().getOptionI18nId();
        }
        return null;
    }

    /**
     * Orders groups by the value the user sees: the entity name for user and twin keys, the localized
     * i18n name for status, class and datalist-option keys, and the scalar itself for text, date and
     * boolean keys. Equal values fall back to the raw key, so the order stays deterministic; the
     * empty key is always last.
     */
    public static Comparator<TwinGroup> values(boolean ascending, Map<UUID, String> localizedNames) {
        return (left, right) -> {
            int byValue = compareRaw(displayValue(left, localizedNames), displayValue(right, localizedNames), ascending);
            if (byValue != 0) {
                return byValue;
            }
            return compareRaw(left.getKey(), right.getKey(), true);
        };
    }

    private static Object displayValue(TwinGroup group, Map<UUID, String> localizedNames) {
        if (group.getKeyValue() instanceof FieldValueUserSingle user && user.getValue() != null) {
            return user.getValue().getName();
        }
        if (group.getKeyValue() instanceof FieldValueLinkSingle link && link.getValue() != null) {
            return link.getValue().getName();
        }
        if (group.getKeyValue() instanceof FieldValueStatus status && status.getValue() != null) {
            return localized(status.getValue().getNameI18nId(), status.getValue().getKey(), localizedNames);
        }
        if (group.getKeyValue() instanceof FieldValueTwinClassSingle twinClass && twinClass.getValue() != null) {
            return localized(twinClass.getValue().getNameI18NId(), twinClass.getValue().getKey(), localizedNames);
        }
        if (group.getKeyValue() instanceof FieldValueSelectSingle option && option.getValue() != null) {
            return localized(option.getValue().getOptionI18nId(), option.getValue().getOption(), localizedNames);
        }
        return group.getKey();
    }

    /**
     * The localized display name with the business key as the fallback: a null i18n id means the
     * entity was never localized, and translateToLocale fills missing translations with empty strings.
     */
    private static String localized(UUID i18nId, String businessKey, Map<UUID, String> localizedNames) {
        String name = i18nId == null ? null : localizedNames.get(i18nId);
        return name == null || name.isBlank() ? businessKey : name;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compareRaw(Object left, Object right, boolean ascending) {
        if (left == null || right == null) {
            return nullsLast(left, right);
        }
        int compared;
        if (left instanceof Comparable comparable && left.getClass().isInstance(right)) {
            compared = comparable.compareTo(right);
        } else {
            compared = String.valueOf(left).compareTo(String.valueOf(right));
        }
        return ascending ? compared : -compared;
    }

    private static int nullsLast(Object left, Object right) {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        return 0;
    }

    private static final class Bucket {
        private FieldValueSingle keyValue;
        private final LinkedHashMap<Object, Bucket> children = new LinkedHashMap<>();
        private final List<TwinEntity> twins = new ArrayList<>();
        private int count;
    }
}
