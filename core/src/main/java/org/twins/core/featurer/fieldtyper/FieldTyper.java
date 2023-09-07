package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;

import java.lang.reflect.Array;
import java.util.*;


@FeaturerType(id = 13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper extends Featurer {
    public FieldTypeUIDescriptor getUiDescriptor(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return getUiDescriptor(properties);
    }

    protected abstract FieldTypeUIDescriptor getUiDescriptor(Properties properties);

    public Object serializeValue(HashMap<String, String> fieldTyperParams, Object value) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return serializeValue(properties, value);
    }

    protected Object serializeValue(Properties properties, Object value) {
        return value;
    }

    public FieldValue deserializeValue(HashMap<String, String> fieldTyperParams, Object value) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return deserializeValue(properties, value);
    }

    protected FieldValue deserializeValue(Properties properties, Object value) {
        return new FieldValueText().value(value != null ? value.toString() : "");
    }
}
