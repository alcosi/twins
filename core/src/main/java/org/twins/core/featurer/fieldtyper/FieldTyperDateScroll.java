package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

@Component
@Featurer(id = 1302,
        name = "FieldTyperDateScroll",
        description = "")
public class FieldTyperDateScroll extends FieldTyper<FieldValueDate> {
    @FeaturerParam(name = "pattern", description = "")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("dateScroll")
                .addParam("pattern", pattern.extract(properties));
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueDate value) throws ServiceException {
        if (twinFieldEntity.twinClassField().required() && StringUtils.isEmpty(value.date()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "twinClassField[" + twinFieldEntity.twinClassFieldId() + "] is required");
        String datePatter = pattern.extract(properties);
        if (!GenericValidator.isDate(value.date(), datePatter, true))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "date[" + value.date() + "] for twinClassField[" + twinFieldEntity.twinClassFieldId() + "] does not match pattern[" + datePatter + "]");
        return value.date();
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, Object value) {
        return new FieldValueDate().date(value != null ? validDateOrEmpty(value.toString(), properties) : "");
    }

    public String validDateOrEmpty(String dateStr, Properties properties) {
        if (GenericValidator.isDate(dateStr, pattern.extract(properties), true))
            return dateStr;
        return "";
    }
}
