package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;



@FeaturerType(id = FeaturerTwins.TYPE_53,
        name = "FieldInitializer",
        description = "")
@Slf4j
public abstract class FieldInitializer<D extends FieldDescriptor, T extends FieldValue> extends FeaturerTwins {
    private Class<T> valueType = null;
    private Class<D> descriptorType = null;

    public FieldInitializer() {
        List<Type> collected = collectParameterizedTypes(getClass(), new ArrayList<>());
        for (Type ptType : collected) {
            if (!(ptType instanceof Class<?> cl))
                continue;
            if (FieldDescriptor.class.isAssignableFrom(cl) && descriptorType == null)
                descriptorType = (Class<D>) cl;
            if (FieldValue.class.isAssignableFrom(cl) && valueType == null)
                valueType = (Class<T>) cl;
        }
        if (descriptorType == null || valueType == null)
            throw new RuntimeException("Can not initialize ");
    }

    public void setInitValue(TwinEntity twin, T value) throws ServiceException {
        setInitValue(twin, value, false);
    }

    public void setInitValue(TwinEntity twin, T value, boolean reinitForce) throws ServiceException {
        if (!valueType.isInstance(value)) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, "{} incorrect value type", value.getTwinClassField().logNormal());
        }
        if (value.isAlreadyInitialized())
            return;
        if (reinitForce || value.isUndefined()) {
            Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldInitializerParams());
            setInitValue(properties, twin, value);
            value.setAlreadyInitialized(true);
        }
    }

    protected abstract void setInitValue(Properties properties, TwinEntity twin, T value) throws ServiceException;

    /**
     * override this if you need to add some data to the descriptor
     */
    public void appendDescriptor(Properties properties, D descriptor) throws ServiceException {
    }
}
