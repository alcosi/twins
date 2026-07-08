package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;

/**
 * Marker for the two publisher flavors. Sealed so subscribers can exhaustive
 * switch on it. All publishers within a single {@link TwinClassFieldRecomputeEvent}
 * share the same concrete type — the orchestrator groups OnField and OnAction
 * listeners separately, never mixes them.
 */
public sealed interface PublisherRef permits PublisherByField, PublisherByAction {

    TwinEntity twin();

    boolean async();
}
