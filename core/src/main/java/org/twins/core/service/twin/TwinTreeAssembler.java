package org.twins.core.service.twin;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twin.TwinGroup;
import org.twins.core.domain.twin.TwinGroupCriterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

public final class TwinTreeAssembler {
    private TwinTreeAssembler() {
    }

    public static List<TwinGroup> assemble(List<Leaf> leaves, List<TwinGroupCriterion> criteria) {
        Bucket root = new Bucket(null);
        LinkedHashSet<UUID> seen = new LinkedHashSet<>();
        if (leaves != null) {
            for (Leaf leaf : leaves) {
                if (leaf == null || leaf.twin == null || leaf.twin.getId() == null || !seen.add(leaf.twin.getId())) {
                    continue;
                }
                expand(leaf, criteria.size(), new ArrayList<>(), root);
            }
        }
        return toGroups(root.children.values(), 0, criteria);
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

    private static void insert(Bucket node, List<Object> path, int level, TwinEntity twin) {
        if (level == path.size()) {
            if (node.leafIds.add(twin.getId())) {
                node.twins.add(twin);
            }
            return;
        }
        node.leafIds.add(twin.getId());
        Object key = path.get(level);
        Bucket child = node.children.computeIfAbsent(key, Bucket::new);
        insert(child, path, level + 1, twin);
    }

    private static List<TwinGroup> toGroups(Iterable<Bucket> buckets, int level, List<TwinGroupCriterion> criteria) {
        TwinGroupCriterion criterion = criteria.get(level);
        boolean last = level == criteria.size() - 1;
        List<TwinGroup> groups = new ArrayList<>();
        for (Bucket bucket : buckets) {
            TwinGroup group = new TwinGroup()
                    .setKey(bucket.key)
                    .setCount(bucket.leafIds.size())
                    .setKeyKind(criterion.getKeyKind())
                    .setGroupFieldId(criterion.getFieldId())
                    .setLinkId(criterion.getLinkId())
                    .setDataListOption(criterion.isDataListOption());
            if (last) {
                group.setTwins(bucket.twins);
            } else {
                group.setGroups(toGroups(bucket.children.values(), level + 1, criteria));
            }
            groups.add(group);
        }
        return groups;
    }

    public static final class Leaf {
        private final TwinEntity twin;
        private final List<List<Object>> levelKeys;

        public Leaf(TwinEntity twin, List<List<Object>> levelKeys) {
            this.twin = twin;
            this.levelKeys = levelKeys;
        }
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
