package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageSimple;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Properties;
import java.util.Set;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1340, name = "Sum fields", description = "Sum of fields")
public class FieldTyperCalcSum extends FieldTyper<FieldDescriptorText, FieldValueText, TwinFieldStorageSimple, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "fieldIds", description = "Fields to sum")
    public static final FeaturerParamUUIDSetTwinsTwinClassFieldId fieldIds = new FeaturerParamUUIDSetTwinsTwinClassFieldId("fieldIds");

    @Autowired
    private TwinClassFieldService twinClassFieldService;

    @Override
    protected FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        Kit<TwinFieldSimpleEntity, UUID> twinFieldSimpleKit = twinField.getTwin().getTwinFieldSimpleKit();
        Set<UUID> extractedTwinFields = fieldIds.extract(properties);
        double totalSum = 0.0;
        for (UUID twinFieldId : extractedTwinFields) {
            TwinFieldSimpleEntity twinFieldSimple = twinFieldSimpleKit.get(twinFieldId);
            if (twinFieldSimple != null) {
                Double ret = twinClassFieldService.parseNumericField(twinField.getTwin(), twinFieldId, 0.0);
                totalSum += ret;
            }
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(String.valueOf(totalSum));
    }
}
