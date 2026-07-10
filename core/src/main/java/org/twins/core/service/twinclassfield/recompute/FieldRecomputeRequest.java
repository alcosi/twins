package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.List;

/**
 * "Recompute Mater field {@code subscriberField} on {@code subscriberTwin}
 * taking {@code triggers} into account".
 *
 * <p>One request = one group (one subscriber twin + one subscriber field). The trigger list
 * contains at least one entry. All triggers in the list share the same
 * {@link RecomputeTrigger} concrete type.
 *
 * <p>The orchestrator pre-loads all entities before dispatching — subscriber
 * TwinEntity, subscriber TwinClassFieldEntity and publisher entities are
 * already populated. FieldTyper implementations must not make extra
 * {@code findById} lookups inside {@code recompute(...)}.
 */
public record FieldRecomputeRequest(
        TwinEntity subscriberTwin,
        TwinClassFieldEntity subscriberField,
        List<RecomputeTrigger> triggers
) {}
