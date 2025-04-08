package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;
import org.twins.core.service.history.ChangesRecorder;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.*;

@Component
@Featurer(id = FeaturerTwins.ID_1321,
        name = "BaseText",
        description = "Field typer for base text twin fields (name, description, externalId)")
public class FieldTyperBaseTextField extends FieldTyper<FieldDescriptorText, FieldValueText, TwinEntity, TwinFieldSearchText> {

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        ChangesRecorder<TwinEntity, TwinEntity> changesRecorder = new ChangesRecorder<>(
                twin,
                new TwinEntity(),
                twin, //todo fix me for draft, it should be another recorder
                twinChangesCollector.getHistoryCollector(twin));

        UUID fieldId = value.getTwinClassField().getId();
        String newValue = value.getValue();

        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_NAME)) {
            changesRecorder.getUpdateEntity().setName(newValue);
            twinService.updateTwinName(changesRecorder);
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_DESCRIPTION)) {
            changesRecorder.getUpdateEntity().setDescription(newValue);
            twinService.updateTwinDescription(changesRecorder);
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID)) {
            changesRecorder.getUpdateEntity().setExternalId(newValue);
            twinService.updateTwinExternalId(changesRecorder);
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    value.getTwinClassField().logShort() + " is not a supported base field for " + twin.logNormal());
        }
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        UUID fieldId = twinField.getTwinClassField().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_NAME)) {
            return new FieldValueText(twinField.getTwinClassField()).setValue(twin.getName());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_DESCRIPTION)) {
            return new FieldValueText(twinField.getTwinClassField()).setValue(twin.getDescription());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID)) {
            return new FieldValueText(twinField.getTwinClassField()).setValue(twin.getExternalId());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchText search) throws ServiceException {
        UUID fieldId = search.getTwinClassFieldEntity().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_NAME)) {
            return Specification.where(TwinSpecification.checkFieldText(search, TwinEntity.Fields.name));
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_DESCRIPTION)) {
            return Specification.where(TwinSpecification.checkFieldText(search, TwinEntity.Fields.description));
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID)) {
            return Specification.where(TwinSpecification.checkFieldText(search, TwinEntity.Fields.externalId));
        } else {
            return null;
        }
    }
}
