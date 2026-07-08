package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.List;

/**
 * "Recompute Mater field {@code subscriberField} on {@code subscriberTwin}
 * taking {@code publishers} into account".
 *
 * One event = one group (one subscriber twin + one subscriber field). The
 * publisher list contains at least one entry. All publishers in the list
 * share the same {@link PublisherRef} concrete type.
 *
 * The orchestrator pre-loads all entities before dispatching — subscriber
 * TwinEntity, subscriber TwinClassFieldEntity and publisher entities are
 * already populated. FieldTyper implementations must not make extra
 * {@code findById} lookups inside {@code recompute(...)}.
 */
public record TwinClassFieldRecomputeEvent(
        TwinEntity subscriberTwin,
        TwinClassFieldEntity subscriberField,
        List<PublisherRef> publishers
) {}
