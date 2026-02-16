package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorBoolean;
import org.twins.core.featurer.fieldtyper.value.FieldValueBoolean;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5304,
        name = "Boolean initializer from param",
        description = "")
@Slf4j
public class FieldInitializerBoolean extends FieldInitializer<FieldDescriptorBoolean, FieldValueBoolean> {
    @FeaturerParam(name = "Value", description = "", order = 1, optional = false, defaultValue = "false")
    public static final FeaturerParamBoolean initValue = new FeaturerParamBoolean("value");

    @Override
    protected void initValue(Properties properties, TwinEntity twin, FieldValueBoolean value) throws ServiceException {
        value.setValue(initValue.extract(properties));
    }
}
