package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.twinclassfield.recompute.TwinClassFieldRecomputeEvent;
import org.twins.core.service.twinclassfield.recompute.TwinClassFieldRecomputeSubscriber;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;

public abstract class FieldTyperCalcBinaryMater extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldValueSearchNumeric> implements FieldTyperCalcBinary, FieldTyperCalcMater, TwinClassFieldRecomputeSubscriber {

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric();
    }

    protected abstract BigDecimal calculate(BigDecimal v1, BigDecimal v2, Properties properties) throws ServiceException;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (skipIfEmpty(twin, properties, twinClassFieldService, List.of(firstFieldId.extract(properties), secondFieldId.extract(properties)), value.getTwinClassField())) {
            return;
        }
        if (twinFieldEntity == null) {
            twinFieldEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldEntity);
        }
        var firstValue = twinClassFieldService.getDecimalValue(twin, firstFieldId.extract(properties), BigDecimal.ZERO);
        var secondValue = twinClassFieldService.getDecimalValue(twin, secondFieldId.extract(properties), BigDecimal.ZERO);
        detectValueChange(twinFieldEntity, twinChangesCollector, calculate(firstValue, secondValue, properties));
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldEntity);
    }

    /**
     * Mater-subscriber entry point. Reuses {@link #serializeValue(Properties, TwinEntity, FieldValueText, TwinChangesCollector)}
     * which reads operands through {@code twinClassFieldService.getDecimalValue(...)} — this works in the sync flow
     * where the publisher twin is still pending in the collector and not yet in the DB. MVP implementation; per-type
     * delta-increment overrides are a future optimization (see ai/plans/field-typer-mater-listeners.md §7.11).
     */
    @Override
    public void recompute(TwinClassFieldRecomputeEvent event, TwinChangesCollector collector) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, event.subscriberField().getFieldTyperParams());
        FieldValueText value = new FieldValueText(event.subscriberField());
        serializeValue(properties, event.subscriberTwin(), value, collector);
    }
}
