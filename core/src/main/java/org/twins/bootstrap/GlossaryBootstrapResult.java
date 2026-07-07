package org.twins.bootstrap;

import java.util.List;

/**
 * Summary of one {@link GlossaryBootstrapService#bootstrap()} pass.
 */
public record GlossaryBootstrapResult(
        int created,
        int updated,
        int skipped,
        int linksAdded,
        int linksRemoved,
        int orphansMarkedDeleted,   // Twins transitioned ACTUAL → DELETED (PHASE 2 MARK_DELETED action)
        int orphansRestored,        // Twins transitioned DELETED → ACTUAL (PHASE 2 RESTORE action, .md reappeared)
        List<String> invalidFiles   // filenames that were dropped during parse
) {
    public static GlossaryBootstrapResult empty(List<String> invalidFiles) {
        return new GlossaryBootstrapResult(0, 0, 0, 0, 0, 0, 0, invalidFiles == null ? List.of() : invalidFiles);
    }
}
