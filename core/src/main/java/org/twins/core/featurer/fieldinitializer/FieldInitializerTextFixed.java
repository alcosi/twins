package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5302,
        name = "Text value initializer",
        description = "")
@Slf4j
public class FieldInitializerTextFixed extends FieldInitializer<FieldDescriptorText, FieldValueText> {
    @FeaturerParam(name = "Value", description = "", optional = false, order = 1)
    public static final FeaturerParamString initValue = new FeaturerParamString("value");

    @Override
    protected void setInitValue(Properties properties, TwinEntity twin, FieldValueText value) throws ServiceException {
        value.setValue(initValue.extract(properties));
    }
}
