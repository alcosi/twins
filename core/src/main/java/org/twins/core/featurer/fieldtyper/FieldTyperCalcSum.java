package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDependent;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1340, name = "Sum fields", description = "Sum of fields")
public class FieldTyperCalcSum extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageDependent, TwinFieldSearchNotImplemented> implements FieldTyperScalable, FieldTyperCalc {

    @FeaturerParam(name = "fieldIds", description = "Fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    /**
     * Builds the per-field storage that bulk-loads operand data (persisted + DB-calculated) for this Sum.
     * The sum math itself stays here (see {@link #calculate}); the storage only guarantees the operands
     * are in memory, and does not write this field's value.
     */
    @Override
    public TwinFieldStorage getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        Set<UUID> operandFieldIds = fieldIds.extract(properties);
        return new TwinFieldStorageDependent(
                twinClassFieldEntity.getId(),
                operandFieldIds,
                featurerService,
                twinClassFieldService);
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        // Caching + cycle protection come from FieldTyperCalc; operand data is preloaded by the storage.
        BigDecimal totalSum = computeCalculated(twinField.getTwin(), twinField.getTwinClassFieldId(), properties);
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(scaleAndRound(totalSum, properties).toPlainString());
    }

    @Override
    public BigDecimal calculate(TwinEntity twin, Properties properties) throws ServiceException {
        // Operands are guaranteed loaded by TwinFieldStorageDecimalCalculated; nested in-memory calculated
        // operands are doracalculated lazily via FieldTyperCalc.resolveDependentDecimalValue.
        BigDecimal sum = BigDecimal.ZERO;
        for (TwinClassFieldEntity tcf : twinClassFieldService.findEntitiesSafe(fieldIds.extract(properties))) {
            sum = sum.add(resolveDependentDecimalValue(twin, tcf));
        }
        return sum;
    }
}
