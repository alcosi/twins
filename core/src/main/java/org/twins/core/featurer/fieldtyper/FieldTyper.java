package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.history.HistoryService;
import org.twins.core.service.twin.TwinService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 13,
        name = "FieldTyper",
        description = "Customize format of twin class field")
@Slf4j
public abstract class FieldTyper<D extends FieldDescriptor, T extends FieldValue, S extends TwinFieldStorage> extends Featurer {
    @Lazy
    @Autowired
    HistoryService historyService;

    @Lazy
    @Autowired
    I18nService i18nService;

    @Lazy
    @Autowired
    TwinService twinService;

    private final Class<T> valuetype;
    private final Class<T> descriptorType;
    private final Class<T> storageType;

    public FieldTyper() {
        ParameterizedType pt = getParameterizedType(getClass());
        descriptorType = (Class) pt.getActualTypeArguments()[0];
        valuetype = (Class) pt.getActualTypeArguments()[1];
        storageType = (Class) pt.getActualTypeArguments()[2];
    }

    public Class<T> getValueType() {
        return valuetype;
    }

    public Class<T> getStorageType() {
        return storageType;
    }

    private static ParameterizedType getParameterizedType(Class<?> _class) {
        Type t = _class.getGenericSuperclass();
        if (!(t instanceof ParameterizedType))
            return getParameterizedType((Class<?>) t);
        else
            return (ParameterizedType) t;
    }

    public D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams(), new HashMap<>());
        return getFieldDescriptor(twinClassFieldEntity, properties);
    }

    protected abstract D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException;

    public void serializeValue(TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (!valuetype.isInstance(value)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT);
        }
        Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldTyperParams(), new HashMap<>());
        serializeValue(properties, twin, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinEntity twin, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;


    public T deserializeValue(TwinField twinField) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinField.getTwinClassField().getFieldTyperParams(), new HashMap<>());
        return (T) deserializeValue(properties, twinField).setTwinClassField(twinField.getTwinClassField());
    }

    protected abstract T deserializeValue(Properties properties, TwinField twinField) throws ServiceException;
}
