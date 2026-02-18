package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageDecimal;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1340, name = "Sum fields", description = "Sum of fields")
public class FieldTyperCalcSum extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageDecimal, TwinFieldSearchNotImplemented> implements FieldTyperScalable {

    @FeaturerParam(name = "fieldIds", description = "Fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var extractedTwinFields = fieldIds.extract(properties);
        var totalSum = BigDecimal.ZERO;

        for (UUID twinFieldId : extractedTwinFields) {
            var value = twinClassFieldService.getDecimalValue(twinField.getTwin(), twinFieldId, BigDecimal.ZERO);
            totalSum = totalSum.add(value);
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(scaleAndRound(totalSum, properties).toPlainString());
    }
}
