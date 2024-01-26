package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.history.HistoryType;
import org.twins.core.dao.history.context.HistoryContextFieldSimpleChange;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
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
    @Lazy
    @Autowired
    I18nService i18nService;

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

    public D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinClassFieldEntity.getFieldTyperParams(), new HashMap<>());
        return getFieldDescriptor(twinClassFieldEntity, properties);
    }

    protected abstract D getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException;

    public void serializeValue(TwinFieldEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (!valuetype.isInstance(value)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_TYPE_INCORRECT);
        }
        if (twinFieldEntity.getId() == null)
            twinChangesCollector.add(twinFieldEntity);
        Properties properties = featurerService.extractProperties(this, twinFieldEntity.getTwinClassField().getFieldTyperParams(), new HashMap<>());
        serializeValue(properties, twinFieldEntity, value, twinChangesCollector);
    }

    protected abstract void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, T value, TwinChangesCollector twinChangesCollector) throws ServiceException;

    protected void detectValueChange(TwinFieldEntity twinFieldEntity, TwinChangesCollector twinChangesCollector, String newValue) {
        if (twinChangesCollector.isChanged(twinFieldEntity, "field[" + twinFieldEntity.getTwinClassField().getKey() + "]", twinFieldEntity.getValue(), newValue)) {
            twinChangesCollector.getHistoryCollector().add(twinFieldEntity.getTwin(), HistoryType.fieldChanged, new HistoryContextFieldSimpleChange()
                    .setFromValue(twinFieldEntity.getValue())
                    .setToValue(newValue)
                    .shotField(twinFieldEntity.getTwinClassField(), i18nService));
            twinFieldEntity.setValue(newValue);
        }
    }

    public T deserializeValue(TwinFieldEntity twinFieldEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, twinFieldEntity.getTwinClassField().getFieldTyperParams(), new HashMap<>());
        return (T) deserializeValue(properties, twinFieldEntity).setTwinClassField(twinFieldEntity.getTwinClassField());
    }

    protected abstract T deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) throws ServiceException;
}
