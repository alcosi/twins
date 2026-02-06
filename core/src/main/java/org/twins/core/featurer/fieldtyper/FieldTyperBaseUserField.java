package org.twins.core.featurer.fieldtyper;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUser;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorageTwin;
import org.twins.core.featurer.fieldtyper.value.FieldValueUserSingle;

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.*;

@Component
@Featurer(id = FeaturerTwins.ID_1322,
        name = "BaseUser",
        description = "Field typer for base user twin fields (creator, assignee. owner)")
public class FieldTyperBaseUserField extends FieldTyper<FieldDescriptorUser, FieldValueUserSingle, TwinFieldStorageTwin, TwinFieldSearchNotImplemented> {

    @Override
    public FieldDescriptorUser getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorUser()
                .multiple(false);
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueUserSingle value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        UUID fieldId = value.getTwinClassField().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            twin.setAssignerUser(value.getValue());
            twin.setAssignerUserId(value.getValue() != null ? value.getValue().getId() : null);
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            twin.setOwnerUser(value.getValue());
            twin.setOwnerUserId(value.getValue() != null ? value.getValue().getId() : null);
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_FIELD_IMMUTABLE, value.getTwinClassField().logShort() + " can not be changed by field typer");
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Field [" + value.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
        }
    }

    @Override
    protected FieldValueUserSingle deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        TwinEntity twin = twinField.getTwin();
        UUID fieldId = twinField.getTwinClassField().getId();
        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_ASSIGNEE_USER)) {
            return new FieldValueUserSingle(twinField.getTwinClassField()).setValue(twin.getAssignerUser());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_CREATOR_USER)) {
            return new FieldValueUserSingle(twinField.getTwinClassField()).setValue(twin.getCreatedByUser());
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_OWNER_USER)) {
            return new FieldValueUserSingle(twinField.getTwinClassField()).setValue(twin.getOwnerUser());
        }
        throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                "Field [" + twinField.getTwinClassField().logShort() + "] is not a supported base field for " + twin.logNormal());
    }
}
