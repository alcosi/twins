package org.twins.core.domain.search;

import org.cambium.common.exception.ServiceException;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.ArrayList;
import java.util.List;

/**
 * Page window over a concatenation of segments. Each segment keeps its own order;
 * the page is a slice of the concatenated sequence.
 */
public final class ConcatenatedPageWindow {
    private ConcatenatedPageWindow() {
    }

    public record Slice(int index, int offset, int limit) {
    }

    public static long total(long[] counts) {
        long total = 0;
        for (long count : counts)
            total += count;
        return total;
    }

    /**
     * @param counts segment sizes, in concatenation order
     * @param offset offset into the concatenation
     * @param limit  maximum number of rows in the page
     * @return overlapping segment windows, in concatenation order. Segments that do not overlap the page are omitted.
     */
    public static List<Slice> slices(long[] counts, int offset, int limit) throws ServiceException {
        if (limit < 1)
            throw new ServiceException(ErrorCodeTwins.PAGINATION_LIMIT_ERROR);
        if (offset < 0)
            throw new ServiceException(ErrorCodeTwins.PAGINATION_ERROR, "pagination offset cannot be negative");
        List<Slice> slices = new ArrayList<>();
        long prefix = 0;
        long pageEnd = (long) offset + limit;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] < 0)
                throw new ServiceException(ErrorCodeTwins.TWIN_SEARCH_CONFIG_INCORRECT, "negative search count");
            long segmentStart = prefix;
            long segmentEnd = prefix + counts[i];
            long overlapStart = Math.max(offset, segmentStart);
            long overlapEnd = Math.min(pageEnd, segmentEnd);
            if (overlapStart < overlapEnd) {
                long segmentOffset = overlapStart - segmentStart;
                long segmentLimit = overlapEnd - overlapStart;
                if (segmentOffset > Integer.MAX_VALUE || segmentLimit > Integer.MAX_VALUE)
                    throw new ServiceException(ErrorCodeTwins.PAGINATION_ERROR, "pagination window exceeds integer range");
                slices.add(new Slice(i, (int) segmentOffset, (int) segmentLimit));
            }
            prefix = segmentEnd;
            if (prefix >= pageEnd)
                break;
        }
        return slices;
    }
}
