package org.twins.core.service.twin;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupLevel;

import java.util.*;

public final class TwinTreeAssembler {
    private TwinTreeAssembler() {
    }

    public static Assembly assemble(List<Leaf> leaves, List<TwinGroupLevel> levels) {
        Bucket root = new Bucket(null);
        LinkedHashSet<UUID> seen = new LinkedHashSet<>();
        if (leaves != null) {
            for (Leaf leaf : leaves) {
                if (leaf == null || leaf.twin == null || leaf.twin.getId() == null || !seen.add(leaf.twin.getId())) {
                    continue;
                }
                expand(leaf, levels.size(), new ArrayList<>(), root);
            }
        }
        List<TwinGroup> all = new ArrayList<>();
        List<TwinGroup> roots = toGroups(root.children.values(), 0, levels, all);
        return new Assembly(roots, all);
    }

    private static void expand(Leaf leaf, int depth, List<Object> path, Bucket root) {
        if (path.size() == depth) {
            insert(root, path, 0, leaf.twin);
            return;
        }
        List<Object> keys = leaf.levelKeys == null || path.size() >= leaf.levelKeys.size()
                ? Collections.singletonList(null)
                : leaf.levelKeys.get(path.size());
        if (keys == null || keys.isEmpty()) {
            keys = Collections.singletonList(null);
        }
        for (Object key : keys) {
            path.add(key);
            expand(leaf, depth, path, root);
            path.remove(path.size() - 1);
        }
    }

    private static void insert(Bucket node, List<Object> path, int levelIndex, TwinEntity twin) {
        if (levelIndex == path.size()) {
            if (node.leafIds.add(twin.getId())) {
                node.twins.add(twin);
            }
            return;
        }
        node.leafIds.add(twin.getId());
        Object key = path.get(levelIndex);
        Bucket child = node.children.computeIfAbsent(key, Bucket::new);
        insert(child, path, levelIndex + 1, twin);
    }

    private static List<TwinGroup> toGroups(Iterable<Bucket> buckets, int levelIndex, List<TwinGroupLevel> levels, List<TwinGroup> all) {
        TwinGroupLevel level = levels.get(levelIndex);
        boolean last = levelIndex == levels.size() - 1;
        List<TwinGroup> groups = new ArrayList<>();
        for (Bucket bucket : buckets) {
            TwinGroup group = new TwinGroup()
                    .setKey(bucket.key)
                    .setCount(bucket.leafIds.size())
                    .setKeyType(level.getKeyType());
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
     * The roots in grouping order, plus every group of every level as a flat list (for batch key
     * entity loading without a second tree walk).
     */
    public record Assembly(List<TwinGroup> roots, List<TwinGroup> all) {
    }

    public static final class Leaf {
        private final TwinEntity twin;
        private final List<List<Object>> levelKeys;

        public Leaf(TwinEntity twin, List<List<Object>> levelKeys) {
            this.twin = twin;
            this.levelKeys = levelKeys;
        }
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

    public static Comparator<TwinGroup> names(Map<UUID, String> names, boolean ascending) {
        return (left, right) -> {
            if (left.getKey() == null || right.getKey() == null) {
                return nullsLast(left.getKey(), right.getKey());
            }
            String leftName = names.get(left.getKey());
            String rightName = names.get(right.getKey());
            int byName = compareText(leftName, rightName, ascending);
            if (byName != 0) {
                return byName;
            }
            return ((UUID) left.getKey()).compareTo((UUID) right.getKey());
        };
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
        private final Object key;
        private final LinkedHashMap<Object, Bucket> children = new LinkedHashMap<>();
        private final LinkedHashSet<UUID> leafIds = new LinkedHashSet<>();
        private final List<TwinEntity> twins = new ArrayList<>();

        private Bucket(Object key) {
            this.key = key;
        }
    }
}
