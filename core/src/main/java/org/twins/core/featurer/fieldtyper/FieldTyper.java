package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


@FeaturerType(id = 13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper<T extends FieldValue> extends Featurer {
    private final Class<T> type;

    public FieldTyper() {
        ParameterizedType pt = getParameterizedType(getClass());
        type = (Class) pt.getActualTypeArguments()[0];
    }

    private static ParameterizedType getParameterizedType(Class<?> _class) {
        Type t = _class.getGenericSuperclass();
        if (!(t instanceof ParameterizedType))
            return getParameterizedType((Class<?>) t);
        else
            return (ParameterizedType) t;
    }

    public FieldTypeUIDescriptor getUiDescriptor(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return getUiDescriptor(properties);
    }

    protected abstract FieldTypeUIDescriptor getUiDescriptor(Properties properties);

    public String serializeValue(TwinFieldEntity twinFieldEntity, T value) throws ServiceException {
        if (!type.isInstance(value)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT);
        }
        Properties properties = featurerService.extractProperties(this, twinFieldEntity.twinClassField().fieldTyperParams(), new HashMap<>());
        return serializeValue(properties, twinFieldEntity, value);
    }

    protected abstract String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, T value) throws ServiceException;

    public T deserializeValue(HashMap<String, String> fieldTyperParams, Object value) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return deserializeValue(properties, value);
    }

    protected abstract T deserializeValue(Properties properties, Object value);
}
