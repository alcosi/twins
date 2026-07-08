package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;

/**
 * Source of a recompute initiation. Sealed so subscribers can exhaustive
 * switch on it. All triggers within a single {@link FieldRecomputeRequest}
 * share the same concrete type — the orchestrator groups OnField and OnAction
 * listeners separately, never mixes them.
 *
 * <p>The name {@code DependencyTrigger} (rather than e.g. {@code PublisherRef}) is deliberate:
 * this abstraction tells a future delta-algorithm <em>why</em> the recompute was requested —
 * without knowing the trigger, delta is impossible (you wouldn't know which publisher contributed).
 *
 * <p>The sealed hierarchy is intentionally extensible. Future trigger types — {@code LinkTrigger},
 * {@code TagTrigger}, {@code MarkerTrigger}, {@code PermissionTrigger} — extend
 * {@code DependencyTrigger} without touching the boundary between resolver and executor.
 */
public sealed interface RecomputeTrigger permits RecomputeTriggerOnField, RecomputeTriggerOnAction {

    TwinEntity publisherTwin();

    boolean async();
}
