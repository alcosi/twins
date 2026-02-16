package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUrl;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUrl;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5307,
        name = "Url value initializer",
        description = "")
@Slf4j
public class FieldInitializerUrlFixed extends FieldInitializer<FieldDescriptorUrl, FieldValueText> {
    @FeaturerParam(name = "Value", description = "", optional = false, order = 1)
    public static final FeaturerParamUrl initValue = new FeaturerParamUrl("value");

    @Override
    protected void initValue(Properties properties, TwinEntity twin, FieldValueText value) throws ServiceException {
        value.setValue(initValue.extract(properties));
    }
}
