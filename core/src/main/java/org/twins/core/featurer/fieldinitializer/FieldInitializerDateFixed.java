package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.DateUtils;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5306,
        name = "Date value initializer",
        description = "")
@Slf4j
public class FieldInitializerDateFixed extends FieldInitializer<FieldDescriptorDate, FieldValueDate> {
    @FeaturerParam(name = "Value", description = "", optional = false, order = 1)
    public static final FeaturerParamString initValue = new FeaturerParamString("value");

    @FeaturerParam(name = "Pattern", description = "", optional = true, order = 1, defaultValue = DateUtils.DEFAULT_DATE_TIME_PATTERN)
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @Override
    protected void setInitValue(Properties properties, TwinEntity twin, FieldValueDate value) throws ServiceException {
        value.setDate(DateUtils.parseDateTime(initValue.extract(properties), pattern.extract(properties)));
    }
}
