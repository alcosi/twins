package org.cambium.common.util;

import org.apache.commons.lang3.Range;

import java.util.*;
import java.util.stream.Collectors;

public class LTreeUtils {
    public static String matchWithDepthRangeBetweenElements(String firstId, String secondId, Range<Integer> depthBetweenIds) {
        if (depthBetweenIds == null) depthBetweenIds = Range.of(0, 1);
        firstId = firstId.replace("-", "_");
        secondId = secondId.replace("-", "_");
        return "*." + firstId + ".*{" + depthBetweenIds.getMinimum() + "," + depthBetweenIds.getMaximum() + "}." + secondId + ".*";
    }

    public static String findChildsLQuery(Collection<String> ids, Range<Integer> depth) {
        return matchWithDepthRange(String.join("|", ids), null, adjustDepthRange(depth));
    }

    public static String matchWithDepthRange(String id, Range<Integer> depthLeft, Range<Integer> depthRight) {
        if (id == null) return null;
        String leftPart = (depthLeft == null) ? "*." : "*{" + depthLeft.getMinimum() + "," + depthLeft.getMaximum() + "}.";
        String rightPart = (depthRight == null) ? ".*" : ".*{" + Math.max(0, depthRight.getMinimum()) + "," + depthRight.getMaximum() + "}";
        id = id.replace("-", "_");
        return leftPart + id + rightPart;
    }

    private static Range<Integer> adjustDepthRange(Range<Integer> depth) {
        if (depth == null) {
            return Range.of(1, (int) Short.MAX_VALUE);
        }
        int min = Math.max(0, depth.getMinimum()); // 0 - if we need to search self
        return Range.of(min, depth.getMaximum());
    }

    public static String matchInTheMiddle(UUID id) {
        return "*." + id.toString().replace("-", "_") + ".*";
    }

    public static String convertToLTreeFormat(UUID uuid) {
        return uuid.toString().replace("-", "_");
    }

    public static String convertToChainLTreeFormat(UUID... uuids) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < uuids.length; i++) {
            result.append(convertToLTreeFormat(uuids[i]));
            if (i < uuids.length - 1) {
                result.append(".");
            }
        }
        return result.toString();
    }

    public static String buildLQueryFromUuids(Collection<UUID> uuids) {
        if (uuids == null || uuids.isEmpty()) {
            return "";
        }
        return uuids.stream()
                .map(LTreeUtils::matchInTheMiddle)
                .collect(Collectors.joining("|"));
    }

    public static String convertUuidFromLTreeFormat(String uuidLtreeFormat) {
        return uuidLtreeFormat.replace("_", "-");
    }

    public static Set<UUID> toUuidsSortedSet(String uuidLTreeFormat, boolean reverse) {
        if (StringUtils.isBlank(uuidLTreeFormat))
            return Collections.emptySet();
        var idsStr = uuidLTreeFormat.replace("_", "-").split("\\.");
        var ret = new LinkedHashSet<UUID>();
        if (reverse) {
            for (int i = idsStr.length - 1; i >= 0; i--)
                ret.add(UUID.fromString(idsStr[i]));
        } else {
            for (var id : idsStr)
                ret.add(UUID.fromString(id));
        }
        return ret;
    }
}
