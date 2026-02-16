package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.time.LocalDateTime;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5309,
        name = "Current date time",
        description = "")
@Slf4j
public class FieldInitializerDateCurrent extends FieldInitializer<FieldDescriptorDate, FieldValueDate> {
    @Override
    protected void initValue(Properties properties, TwinEntity twin, FieldValueDate value) throws ServiceException {
        value.setDate(LocalDateTime.now());
    }
}
