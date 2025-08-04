package org.cambium.featurer.params;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerParamType;
import org.twins.core.featurer.fieldtyper.FieldTyperTextField;

import java.util.Properties;

@FeaturerParamType(
        id = "EDITOR_TYPE",
        description = "string twins editor type",
        regexp = FeaturerParamStringTwinsEditorType.EDITOR_TYPE_REGEXP,
        example = "PLAIN")
public class FeaturerParamStringTwinsEditorType extends FeaturerParam<FieldTyperTextField.TextEditorType> {
    public static final String EDITOR_TYPE_REGEXP = "PLAIN|MARKDOWN_GITHUB|MARKDOWN_BASIC|HTML";

    public FeaturerParamStringTwinsEditorType(String key) {super(key);}

    @Override
    public FieldTyperTextField.TextEditorType extract(Properties properties) {
        String value = (String) properties.get(key);
        return value != null ?
                FieldTyperTextField.TextEditorType.valueOf(value) :
                FieldTyperTextField.TextEditorType.PLAIN;
    }

    @Override
    public void validate(String value) throws ServiceException {
        if (value == null || !value.matches(EDITOR_TYPE_REGEXP)) {
            throw new ServiceException(ErrorCodeCommon.FEATURER_WRONG_PARAMS,
                    "param[" + key + "] value[" + value + "] must be one of: " + EDITOR_TYPE_REGEXP);
        }
    }
}
