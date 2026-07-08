package org.twins.core.service.twinclassfield.recompute;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.TwinChangesCollector;

/**
 * Contract for a FieldTyper that acts as a Mater-subscriber: receives
 * notifications from the recompute orchestrator when a publisher field changes
 * or a publisher action fires, and recomputes its own Mater value.
 *
 * The implementation chooses how to recompute:
 * - full recompute via serializeValue (default in FieldTyperCalcBinaryMater)
 * - delta-increment through the collector (override, future)
 * - skip (e.g. operand missing and skipIfEmpty=false)
 *
 * Changes must go into the supplied collector so they share the publisher tx
 * (sync flow) or the worker tx (async flow). Implementations update exactly
 * one field — the subscriber field from {@link TwinClassFieldRecomputeEvent}
 * — so the contract returns void.
 *
 * The FieldTyper should not perform any extra lookups: subscriber TwinEntity,
 * subscriber TwinClassFieldEntity and publisher entities are already loaded
 * and passed via {@link TwinClassFieldRecomputeEvent}.
 */
public interface TwinClassFieldRecomputeSubscriber {

    /**
     * Recompute the Mater field {@code event.subscriberField()} on
     * {@code event.subscriberTwin()} and write the change into {@code collector}.
     *
     * @param event     full context with already-loaded entities
     * @param collector accumulate point for Mater field changes
     */
    void recompute(TwinClassFieldRecomputeEvent event, TwinChangesCollector collector) throws ServiceException;
}
