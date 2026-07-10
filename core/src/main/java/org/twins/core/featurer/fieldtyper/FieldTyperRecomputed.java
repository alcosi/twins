package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.service.twinclassfield.recompute.FieldRecomputeRequest;

/**
 * Contract for a FieldTyper that acts as a Mater-subscriber: receives
 * requests from the recompute orchestrator when a publisher field changes
 * or a publisher action fires, and recomputes its own Mater value.
 *
 * The implementation chooses how to recompute:
 * - full recompute via serializeValue (default in FieldTyperCalcBinaryMater)
 * - delta-increment through the collector (override, future)
 * - skip (e.g. operand missing and skipIfEmpty=false)
 *
 * Changes must go into the supplied collector so they share the publisher tx
 * (sync flow) or the worker tx (async flow). Implementations update exactly
 * one field — the subscriber field from {@link FieldRecomputeRequest}
 * — so the contract returns void.
 *
 * The FieldTyper should not perform any extra lookups: subscriber TwinEntity,
 * subscriber TwinClassFieldEntity and publisher entities are already loaded
 * and passed via {@link FieldRecomputeRequest}.
 */
public interface FieldTyperRecomputed {

    /**
     * Recompute the Mater field {@code request.subscriberField()} on
     * {@code request.subscriberTwin()} and write the change into {@code collector}.
     *
     * @param request   full context with already-loaded entities
     * @param collector accumulate point for Mater field changes
     */
    void recompute(FieldRecomputeRequest request, TwinChangesCollector collector) throws ServiceException;
}
