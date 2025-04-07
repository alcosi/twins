package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
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

import java.util.Properties;
import java.util.UUID;

import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_DESCRIPTION;
import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID;
import static org.twins.core.service.SystemEntityService.TWIN_CLASS_FIELD_TWIN_NAME;

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
        UUID fieldId = value.getTwinClassField().getId();
        String newValue = value.getValue();

        if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_NAME)) {
            if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.name, twin.getName(), newValue)) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(
                            historyService.nameChanged(twin.getName(), newValue));
                }
                twin.setName(newValue);
            }
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_DESCRIPTION)) {
            if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.description, twin.getDescription(), newValue)) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(
                            historyService.descriptionChanged(twin.getDescription(), newValue));
                }
                twin.setDescription(newValue);
            }
        } else if (fieldId.equals(TWIN_CLASS_FIELD_TWIN_EXTERNAL_ID)) {
            if (twinChangesCollector.collectIfChanged(twin, TwinEntity.Fields.externalId, twin.getExternalId(), newValue)) {
                if (twinChangesCollector.isHistoryCollectorEnabled()) {
                    twinChangesCollector.getHistoryCollector(twin).add(
                            historyService.externalIdChanged(twin.getExternalId(), newValue));
                }
                twin.setExternalId(newValue);
            }
        } else {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    value.getTwinClassField().logShort() + " is not a supported base field for " + twin.logNormal());
        }
        if (value.getTwinClassField().getRequired() && StringUtils.isEmpty(newValue)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    value.getTwinClassField().logShort() + " is required");
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
