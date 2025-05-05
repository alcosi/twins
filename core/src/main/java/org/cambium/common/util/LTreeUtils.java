package org.cambium.common.util;

import org.apache.commons.lang3.Range;

import java.util.Collection;
import java.util.UUID;

public class LTreeUtils {
    public static String matchWithDepthRangeBetweenElements(String firstId, String secondId, Range<Integer> depthBetweenIds) {
        if (depthBetweenIds == null) depthBetweenIds = Range.of(0, 1);
        firstId = firstId.replace("-", "_");
        secondId = secondId.replace("-", "_");
        return "*." + firstId + ".*{" + depthBetweenIds.getMinimum() + "," + depthBetweenIds.getMaximum() + "}." + secondId + ".*";
    }


    public static String findChildsLQuery(Collection<String> ids, Range<Integer> depth) {
        if (depth == null) depth = Range.of(0, 1);
        return matchWithDepthRange(String.join("|", ids), null, depth);
    }

    public static String matchWithDepthRange(String id, Range<Integer> depthLeft, Range<Integer> depthRight) {
        if (id == null) return null;
        String leftPart;
        if (depthLeft == null) {
            leftPart = "*.";
        } else {
            leftPart = "*{" + depthLeft.getMinimum() + "," + depthLeft.getMaximum() + "}.";
        }
        String rightPart;
        if (depthRight == null) {
            rightPart = ".*";
        } else {
            rightPart = ".*{" + depthRight.getMinimum() + "," + depthRight.getMaximum() + "}";
        }
        id = id.replace("-", "_");
        return leftPart + id + rightPart;
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
}
