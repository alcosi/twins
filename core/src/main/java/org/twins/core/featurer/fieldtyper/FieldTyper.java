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
public abstract class FieldTyper<T extends FieldValue> extends Featurer {
    public FieldTypeUIDescriptor getUiDescriptor(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return getUiDescriptor(properties);
    }

    protected abstract FieldTypeUIDescriptor getUiDescriptor(Properties properties);

    public String serializeValue(HashMap<String, String> fieldTyperParams, T value) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return serializeValue(properties, value);
    }

    protected abstract String serializeValue(Properties properties, T value);

    public T deserializeValue(HashMap<String, String> fieldTyperParams, Object value) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return deserializeValue(properties, value);
    }

    protected abstract T deserializeValue(Properties properties, Object value);
}
