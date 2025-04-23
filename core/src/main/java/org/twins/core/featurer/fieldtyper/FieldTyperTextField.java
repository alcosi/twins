package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.cambium.featurer.params.FeaturerParamStringTwinsEditorType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
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
    @FeaturerParam(name = "Regexp", description = "", order = 2)
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");
    @FeaturerParam(name = "contentType", description = "", order = 3, optional = true, defaultValue = "PLAIN")
    public static final FeaturerParamStringTwinsEditorType contentType = new FeaturerParamStringTwinsEditorType("contentType");

    @Override
    public FieldDescriptorText getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorText()
                .regExp(regexp.extract(properties))
                .editorType(contentType.extract(properties).toString());
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value.getValue()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        String pattern = regexp.extract(properties);
        if (!value.getValue().matches(pattern))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " value[" + value.getValue() + "] does not match pattern[" + pattern + "]");
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchText search) {
        return Specification.where(TwinSpecification.checkFieldText(search, TwinEntity.Fields.fieldsSimple, TwinFieldSimpleEntity.Fields.value));
    }

    public enum TextEditorType {
        PLAIN,
        MARKDOWN_GITHUB,
        MARKDOWN_BASIC;

        @Override
        public String toString() {
            return name();
        }
    }
}
