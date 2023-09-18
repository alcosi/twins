package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper<D extends FieldDescriptor, T extends FieldValue> extends Featurer {
    private final Class<T> valuetype;
    private final Class<T> descriptorType;

    public FieldTyper() {
        ParameterizedType pt = getParameterizedType(getClass());
        descriptorType = (Class) pt.getActualTypeArguments()[0];
        valuetype = (Class) pt.getActualTypeArguments()[1];
    }

    public Class<T> getValueType() {
        return valuetype;
    }

    private static ParameterizedType getParameterizedType(Class<?> _class) {
        Type t = _class.getGenericSuperclass();
        if (!(t instanceof ParameterizedType))
            return getParameterizedType((Class<?>) t);
        else
            return (ParameterizedType) t;
    }

    public D getFieldDescriptor(HashMap<String, String> fieldTyperParams) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fieldTyperParams, new HashMap<>());
        return getFieldDescriptor(properties);
    }

    protected abstract D getFieldDescriptor(Properties properties);

    public String serializeValue(TwinFieldEntity twinFieldEntity, T value) throws ServiceException {
        if (!valuetype.isInstance(value)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT);
        }
        Properties properties = featurerService.extractProperties(this, twinFieldEntity.twinClassField().getFieldTyperParams(), new HashMap<>());
        return serializeValue(properties, twinFieldEntity, value);
    }

    protected abstract String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, T value) throws ServiceException;

    public T deserializeValue(TwinFieldEntity twinFieldEntity, Object value) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinFieldEntity.twinClassField().getFieldTyperParams(), new HashMap<>());
        return (T) deserializeValue(properties, twinFieldEntity, value).setTwinClassField(twinFieldEntity.twinClassField());
    }

    protected abstract T deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity, Object value);
}
