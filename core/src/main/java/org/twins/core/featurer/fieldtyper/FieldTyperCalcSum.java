package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.math.BigDecimal;
import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_1340,
        name = "Sum fields",
        description = "Sum of fields"
)
public class FieldTyperCalcSum extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldStorageDecimal, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "fieldIds", description = "Fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldDecimalKit = twinField.getTwin().getTwinFieldDecimalKit();
        var extractedTwinFields = fieldIds.extract(properties);
        var totalSum = BigDecimal.ZERO;

        for (var twinFieldId : extractedTwinFields) {
            var twinFieldDecimal = twinFieldDecimalKit.get(twinFieldId);

            if (twinFieldDecimal != null) {
                totalSum = totalSum.add(twinFieldDecimal.getValue());
            }
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(totalSum.toString());
    }
}
