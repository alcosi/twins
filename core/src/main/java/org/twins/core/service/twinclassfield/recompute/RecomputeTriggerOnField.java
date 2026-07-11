package org.twins.core.service.twinclassfield.recompute;

import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

/**
 * Trigger for an OnField recompute: the value of {@code publisherField} has changed on
 * {@code publisherTwin}. Both entities are already loaded; no extra lookup is required.
 *
 * <p>IMPORTANT (sync flow): at recompute time the publisher twin is already mutated inside the
 * {@link org.twins.core.domain.TwinChangesCollector} but not yet committed to the DB.
 * {@code serializeValue} reads operands through {@code twinClassFieldService.getDecimalValue(...)}
 * which knows how to read the pending state from the collector — that is why full recompute works
 * synchronously.
 */
public record RecomputeTriggerOnField(
        TwinEntity publisherTwin,
        TwinClassFieldEntity publisherField,
        boolean async
) implements RecomputeTrigger {

    @Override
    public String publisherKey() {
        return "field:" + publisherTwin.getId() + ":" + publisherField.getId();
    }
}
