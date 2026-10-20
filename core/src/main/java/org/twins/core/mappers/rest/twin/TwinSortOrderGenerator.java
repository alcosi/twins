package org.twins.core.mappers.rest.twin;

import java.sql.Timestamp;

/**
 * Temporary sort key for API responses while twin.sort_order / twin_link.sort_order are not
 * persisted yet (docs/backlog/045): 3 letters A-Z from createdAt (hourly buckets since
 * 2026-01-01 UTC; ~2 years horizon, then clips at "ZZZ"). Lexicographic order == creation order.
 * TODO: remove once sort_order is stored in DB and mapped from the entity.
 */
public class TwinSortOrderGenerator {
    private static final long ANCHOR = 1_767_225_600_000L; // 2026-01-01T00:00:00Z

    private TwinSortOrderGenerator() {
    }

    public static String generate(Timestamp createdAt) {
        long bucket = ((createdAt != null ? createdAt.getTime() : 0L) - ANCHOR) / 3_600_000L;
        if (bucket < 0)
            bucket = 0;
        if (bucket > 17_575)
            bucket = 17_575;
        return "" + (char) ('A' + bucket / 676)
                + (char) ('A' + bucket / 26 % 26)
                + (char) ('A' + bucket % 26);
    }
}
