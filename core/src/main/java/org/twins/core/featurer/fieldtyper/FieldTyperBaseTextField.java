package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.*;

@Component
@Featurer(id = FeaturerTwins.ID_1321,
        name = "BaseText",
        description = "Field typer for base text twin fields (name, description, externalId)")
public class FieldTyperBaseTextField extends FieldTyperImmutable<FieldDescriptorText, FieldValueText, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText();
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
}
