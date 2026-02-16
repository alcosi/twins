package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5301,
        name = "Null value initializer",
        description = "")
@Slf4j
public class FieldInitializerNull extends FieldInitializer<FieldDescriptor, FieldValue> {
    @Override
    protected void initValue(Properties properties, TwinEntity twin, FieldValue value) throws ServiceException {
        value.undefine();
    }
}
