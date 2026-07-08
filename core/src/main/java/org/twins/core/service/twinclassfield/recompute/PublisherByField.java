package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

/**
 * Publisher for an OnField recompute: a field value of {@code twinClassField}
 * has changed on {@code twin}. Both entities are already loaded; no extra
 * lookup is required.
 *
 * IMPORTANT (sync flow): at recompute time the publisher twin is already
 * mutated inside the {@link org.twins.core.domain.TwinChangesCollector} but
 * not yet committed to the DB. serializeValue reads operands through
 * {@code twinClassFieldService.getDecimalValue(...)} which knows how to read
 * the pending state from the collector — that is why full recompute works
 * synchronously.
 */
public record PublisherByField(
        TwinEntity twin,
        TwinClassFieldEntity twinClassField,
        boolean async
) implements PublisherRef {}
