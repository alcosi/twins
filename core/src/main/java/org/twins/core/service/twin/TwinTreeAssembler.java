package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.fieldtyper.value.FieldValueLinkSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueSingle;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;

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
     * Sorts every sibling list by its level's sort. NAME sorts read the name of the key entity
     * (users and twins are the only name-sortable keys), so they need the entities attached
     * after assembly; the empty key is always last.
     */
    public static void sort(List<TwinGroup> roots, List<TwinGroupLevel> levels) {
        sort(roots, levels, 0);
    }

    private static void sort(List<TwinGroup> groups, List<TwinGroupLevel> levels, int levelIndex) {
        if (groups == null || groups.isEmpty() || levelIndex >= levels.size()) {
            return;
        }
        groups.sort(order(levels.get(levelIndex)));
        for (TwinGroup group : groups) {
            sort(group.getGroups(), levels, levelIndex + 1);
        }
    }

    private static Comparator<TwinGroup> order(TwinGroupLevel level) {
        return switch (level.getSort()) {
            case NAME_ASC -> names(true);
            case NAME_DESC -> names(false);
            case COUNT_ASC -> counts(true);
            case COUNT_DESC -> counts(false);
            case KEY_ASC -> keys(true);
            case KEY_DESC -> keys(false);
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

    public static Comparator<TwinGroup> names(boolean ascending) {
        return (left, right) -> {
            if (left.getKey() == null || right.getKey() == null) {
                return nullsLast(left.getKey(), right.getKey());
            }
            int byName = compareText(keyName(left), keyName(right), ascending);
            if (byName != 0) {
                return byName;
            }
            return ((UUID) left.getKey()).compareTo((UUID) right.getKey());
        };
    }

    private static String keyName(TwinGroup group) {
        if (group.getKeyValue() instanceof FieldValueUserSingle user && user.getValue() != null) {
            return user.getValue().getName();
        }
        if (group.getKeyValue() instanceof FieldValueLinkSingle link && link.getValue() != null) {
            return link.getValue().getName();
        }
        return null;
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

    private static int compareText(String left, String right, boolean ascending) {
        if (left == null || right == null) {
            return nullsLast(left, right);
        }
        int compared = left.compareTo(right);
        return ascending ? compared : -compared;
    }

    private static final class Bucket {
        private FieldValueSingle keyValue;
        private final LinkedHashMap<Object, Bucket> children = new LinkedHashMap<>();
        private final List<TwinEntity> twins = new ArrayList<>();
        private int count;
    }
}
