package org.twins.core.featurer.fieldtyper;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorPassword;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Log4j2
@Component
@Featurer(id = FeaturerTwins.ID_1326,
        name = "Password",
        description = "Password field")
public class FieldTyperPassword
        extends FieldTyperSimple<FieldDescriptorPassword, FieldValueText, TwinFieldSearchNotImplemented> {

    @FeaturerParam(name = "Regexp", description = "")
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity,
                                  FieldValueText value, TwinChangesCollector twinChangesCollector)
            throws ServiceException {
        if (twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value.getValue())) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,
                    twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required"
            );
        }

        String pattern = regexp.extract(properties);

        if (!value.getValue().matches(pattern)) {
            throw new ServiceException(
                    ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL)
                            + " value["
                            + value.getValue()
                            + "] does not match pattern["
                            + pattern + "]"
            );
        }

        detectValueChange(twinFieldEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField,
                                              TwinFieldSimpleEntity twinFieldEntity) throws ServiceException {
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(
                        twinFieldEntity != null && twinFieldEntity.getValue() != null
                                ? twinFieldEntity.getValue()
                                : null
                );
    }

    @Override
    protected FieldDescriptorPassword getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity,
                                                         Properties properties) throws ServiceException {
        return new FieldDescriptorPassword()
                .regExp(regexp.extract(properties));
    }
}
