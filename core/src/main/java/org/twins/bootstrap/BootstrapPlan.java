package org.twins.bootstrap;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinLinkEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Internal classification produced by {@link GlossaryBootstrapService#discover(List, java.util.UUID)}.
 * Holds the actions to execute in PHASE 2.
 *
 * Each {@link Update} pairs a parsed {@link GlossaryEntityDto} with the existing DB state
 * (TwinEntity + its current see-also links) needed to compute the {@code TwinUpdate} payload.
 */
record BootstrapPlan(
        List<GlossaryEntityDto> creates,
        List<Update> updates,
        List<Update> restores,
        List<TwinEntity> markDeletes,
        int skips
) {
    static BootstrapPlan empty() {
        return new BootstrapPlan(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), 0);
    }

    record Update(GlossaryEntityDto dto, TwinEntity dbTwin, List<TwinLinkEntity> existingLinks) {}
}
