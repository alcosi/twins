package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(
        id = FeaturerTwins.ID_1352,
        name = "Sum fields (saved)",
        description = "Save sum of twin fields on serializeValue, and return saved total from database"
)
public class FieldTyperCalcSumMater extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldValueSearchNumeric> implements FieldTyperScalable, FieldTyperCalcMater {

    @FeaturerParam(name = "fieldIds", description = "Fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (shouldSkipSerializeOnMissingOperands(twin, properties, twinClassFieldService, fieldIds.extract(properties), value.getTwinClassField())) {
            return;
        }
        if (twinFieldEntity == null) {
            twinFieldEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), null);
            twinChangesCollector.add(twinFieldEntity);
        }
        detectValueChange(twinFieldEntity, twinChangesCollector, calcSum(properties, twin));
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldEntity);
    }

    private BigDecimal calcSum(Properties properties, TwinEntity twin) throws ServiceException {
        var totalSum = BigDecimal.ZERO;

        for (UUID twinFieldId : fieldIds.extract(properties)) {
            totalSum = totalSum.add(twinClassFieldService.getDecimalValue(twin, twinFieldId, BigDecimal.ZERO));
        }

        return scaleAndRound(totalSum, properties);
    }
}
