package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamStringTwinsEditorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twin.TwinFieldSimpleRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Slf4j
@Component
@Featurer(id = FeaturerTwins.ID_1301,
        name = "Text",
        description = "")
public class FieldTyperTextField extends FieldTyperSimple<FieldDescriptorText, FieldValueText, TwinFieldSearchText> {
    @FeaturerParam(name = "Regexp", description = "", optional = true, defaultValue = "(?s).*", order = 1)
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @FeaturerParam(name = "EditorType", description = "", order = 2, optional = true, defaultValue = "PLAIN")
    public static final FeaturerParamStringTwinsEditorType editorType = new FeaturerParamStringTwinsEditorType("editorType");

    @FeaturerParam(name = "Unique", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean unique = new FeaturerParamBoolean("unique");

    @FeaturerParam(name = "Default value", description = "", optional = true, order = 1)
    public static final FeaturerParamString defaultValue = new FeaturerParamString("defaultValue");

    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        FieldDescriptorText descriptorText = new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(editorType.extract(properties));
        descriptorText.backendValidated(unique.extract(properties));
        return descriptorText;
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ?
                        twinFieldEntity.getValue() : null);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchText search) {
        return TwinSpecification.checkFieldText(search, TwinEntity.Fields.fieldsSimple, TwinFieldSimpleEntity.Fields.value);
    }

    private void checkForUniqueness(TwinEntity twin, FieldValueText value) throws ServiceException {
        OwnerType ownerType = twin.getTwinClass().getOwnerType();

        switch (ownerType) {
            case USER, DOMAIN_USER -> {
                if (!twinFieldSimpleRepository.existsByTwinClassFieldIdAndValueAndOwnerUserIdExcludingTwin(value.getTwinClassFieldId(), value.getValue(), twin.getOwnerUserId(), twin.getId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_NOT_UNIQUE, value.getTwinClassField().logNormal() + " value[" + value.getValue() + "] is not unique");
                }
            }
            case BUSINESS_ACCOUNT, DOMAIN_BUSINESS_ACCOUNT -> {
                if (!twinFieldSimpleRepository.existsByTwinClassFieldIdAndValueAndOwnerBusinessAccountIdExcludingTwin(value.getTwinClassFieldId(), value.getValue(), twin.getOwnerBusinessAccountId(), twin.getId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_NOT_UNIQUE, value.getTwinClassField().logNormal() + " value[" + value.getValue() + "] is not unique");
                }
            }
            default -> {
                if (!twinFieldSimpleRepository.existsByTwinClassFieldIdAndValueExcludingTwin(value.getTwinClassFieldId(), value.getValue(), twin.getId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_NOT_UNIQUE, value.getTwinClassField().logNormal() + " value[" + value.getValue() + "] is not unique");
                }
            }
        }
    }

    @Override
    public ValidationResult validate(Properties properties, TwinEntity twin, FieldValueText fieldValue) {
        try {
            String pattern = regexp.extract(properties);
            if (!fieldValue.getValue().matches(pattern)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, fieldValue.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " value[" + fieldValue.getValue() + "] does not match pattern[" + pattern + "]");
            }
            if (unique.extract(properties).equals(true)) {
                checkForUniqueness(twin, fieldValue);
            }
        } catch (ServiceException e) {
            return new ValidationResult(false, i18nService.translateToLocale(fieldValue.getTwinClassField().getBeValidationErrorI18nId()));
        }
        return new ValidationResult(true);
    }

    @Override
    protected void setDefaultValueIfConfigured(Properties properties, TwinEntity twin, FieldValueText value) {
        var defaultValueString = defaultValue.extract(properties);
        if (defaultValueString != null) {
            value.setValue(defaultValueString);
        }
    }
}
