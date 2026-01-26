package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_53,
        name = "FieldInitializer",
        description = "")
@Slf4j
public abstract class FieldInitializer<D extends FieldDescriptor, T extends FieldValue> extends FeaturerTwins {
    public void setInitValue(TwinEntity twin, T value) throws ServiceException {
        setInitValue(twin, value, false);
    }

    public void setInitValue(TwinEntity twin, T value, boolean reinitForce) throws ServiceException {
        if (value.isAlreadyInitialized())
            return;
        if (reinitForce || value.isUndefined()) {
            Properties properties = featurerService.extractProperties(this, value.getTwinClassField().getFieldInitializerParams(), new HashMap<>());
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
