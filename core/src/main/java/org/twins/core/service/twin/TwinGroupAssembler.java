package org.twins.core.service.twin;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.twin.TwinGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class TwinGroupAssembler {
    private TwinGroupAssembler() {
    }

    public static List<TwinGroup> assemble(List<TwinEntity> orderedTwins, TwinGroupKeys keys) throws ServiceException {
        Map<UUID, TwinEntity> unique = new LinkedHashMap<>();
        if (orderedTwins != null) {
            for (TwinEntity twin : orderedTwins) {
                if (twin == null || twin.getId() == null) {
                    continue;
                }
                unique.putIfAbsent(twin.getId(), twin);
            }
        }
        Map<Object, TwinGroup> groups = new LinkedHashMap<>();
        for (TwinEntity twin : unique.values()) {
            List<Object> twinKeys = keys.keysOf(twin);
            if (twinKeys == null || twinKeys.isEmpty()) {
                twinKeys = List.of();
            }
            Set<Object> seen = new LinkedHashSet<>();
            boolean any = false;
            for (Object key : twinKeys) {
                if (!seen.add(key)) {
                    continue;
                }
                any = true;
                group(groups, key).getTwins().add(twin);
            }
            if (!any) {
                group(groups, null).getTwins().add(twin);
            }
        }
        List<TwinGroup> result = new ArrayList<>(groups.values());
        for (TwinGroup group : result) {
            group.setCount(group.getTwins().size());
        }
        return result;
    }

    @FunctionalInterface
    public interface TwinGroupKeys {
        List<Object> keysOf(TwinEntity twin) throws ServiceException;
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

    private static TwinGroup group(Map<Object, TwinGroup> groups, Object key) {
        TwinGroup group = groups.get(key);
        if (group == null) {
            group = new TwinGroup().setKey(key).setTwins(new ArrayList<>());
            groups.put(key, group);
        }
        return group;
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
}
