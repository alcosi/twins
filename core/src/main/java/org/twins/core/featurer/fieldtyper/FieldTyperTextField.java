package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
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
import org.twins.core.domain.enum_.twinclass.OwnerType;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchText;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1301,
        name = "Text",
        description = "")
public class FieldTyperTextField extends FieldTyperSimple<FieldDescriptorText, FieldValueText, TwinFieldSearchText> {
    @FeaturerParam(name = "Regexp", description = "", order = 1)
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");
    @FeaturerParam(name = "EditorType", description = "", order = 2, optional = true, defaultValue = "PLAIN")
    public static final FeaturerParamStringTwinsEditorType editorType = new FeaturerParamStringTwinsEditorType("editorType");
    @FeaturerParam(name = "Unique", description = "", order = 3, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean unique = new FeaturerParamBoolean("unique");

    @Autowired
    private TwinFieldSimpleRepository twinFieldSimpleRepository;

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(editorType.extract(properties))
                .unique(unique.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        String pattern = regexp.extract(properties);
        if (!value.getValue().matches(pattern)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " value[" + value.getValue() + "] does not match pattern[" + pattern + "]");
        }

        if (unique.extract(properties).equals(true)) {
            checkForUniqueness(twinFieldEntity, value);
        }

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
        return Specification.where(TwinSpecification.checkFieldText(search, TwinEntity.Fields.fieldsSimple, TwinFieldSimpleEntity.Fields.value));
    }

    private void checkForUniqueness(TwinFieldSimpleEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        OwnerType ownerType = twinFieldEntity.getTwin().getTwinClass().getOwnerType();

        switch (ownerType) {
            case USER, DOMAIN_USER -> {
                if (!twinFieldSimpleRepository.existsByTwinClassFieldIdAndValueAndOwnerUserId(twinFieldEntity.getTwinClassFieldId(), value.getValue(), twinFieldEntity.getTwin().getOwnerUserId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_NOT_UNIQUE, twinFieldEntity.getTwinClassField().logNormal() + " value[" + value.getValue() + "] is not unique");
                }
            }
            case BUSINESS_ACCOUNT, DOMAIN_BUSINESS_ACCOUNT -> {
                if (!twinFieldSimpleRepository.existsByTwinClassFieldIdAndValueAndOwnerBusinessAccountId(twinFieldEntity.getTwinClassFieldId(), value.getValue(), twinFieldEntity.getTwin().getOwnerBusinessAccountId())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_NOT_UNIQUE, twinFieldEntity.getTwinClassField().logNormal() + " value[" + value.getValue() + "] is not unique");
                }
            }
            default -> {
                if (!twinFieldSimpleRepository.existsByTwinClassFieldIdAndValue(twinFieldEntity.getTwinClassFieldId(), value.getValue())) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_IS_NOT_UNIQUE, twinFieldEntity.getTwinClassField().logNormal() + " value[" + value.getValue() + "] is not unique");
                }
            }
        }
    }
}
