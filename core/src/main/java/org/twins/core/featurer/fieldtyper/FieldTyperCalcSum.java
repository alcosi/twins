package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcFieldsSum;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.*;

@Component
@Featurer(id = FeaturerTwins.ID_1320, name = "Sum fields", description = "Sum of fields")
public class FieldTyperCalcSum extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldStorageCalcFieldsSum, TwinFieldSearchNotImplemented> {

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
        List<TwinFieldSimpleEntity> fields = new ArrayList<>();
        Kit<TwinFieldSimpleEntity, UUID> twinFieldSimpleKit = twinField.getTwin().getTwinFieldSimpleKit();
        Set<UUID> extractedTwinFields = fieldIds.extract(properties);
        for (UUID twinFieldId : extractedTwinFields) {
            TwinFieldSimpleEntity twinFieldSimple = twinFieldSimpleKit.get(twinFieldId);
            if (twinFieldSimple != null) {
                fields.add(twinFieldSimple);
            }
        }

        double totalSum = 0.0;
        for (TwinFieldSimpleEntity field : fields) {
            try {
                if (field.getValue() != null) {
                    double val = Double.parseDouble(field.getValue());
                    totalSum += val;
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(String.valueOf(totalSum));
    }
}
