package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageCalcFieldsSum;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.featurer.params.FeaturerParamUUIDSetTwinsTwinClassFieldId;

import java.util.Properties;

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
        Object val = twinField.getTwin().getTwinFieldCalculated().get(twinField.getTwinClassFieldId());
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(val != null ? String.valueOf(val) : "0");
    }

    @Override
    public TwinFieldStorageCalcFieldsSum getStorage(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        return new TwinFieldStorageCalcFieldsSum(
                twinClassFieldEntity.getId(),
                fieldIds.extract(properties));
    }
}
