package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListOptionId;
import org.twins.core.service.datalist.DataListOptionService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5303,
        name = "Option initializer from param",
        description = "")
@Slf4j
public class FieldInitializerList extends FieldInitializer<FieldDescriptorList, FieldValueSelect> {
    @FeaturerParam(name = "Value", description = "Init value", order = 1, optional = false)
    public static final FeaturerParamUUID initValue = new FeaturerParamUUIDTwinsDataListOptionId("defaultOptionId");

    @Autowired
    DataListOptionService dataListOptionService;

    @Override
    protected void setInitValue(Properties properties, TwinEntity twin, FieldValueSelect value) throws ServiceException {
        DataListOptionEntity initOption = dataListOptionService.findEntitySafe(initValue.extract(properties));
        value.clear();
        value.add(initOption);
    }

    @Override
    public void appendDescriptor(Properties properties, FieldDescriptorList descriptor) throws ServiceException {
        descriptor.defaultDataListOptionId(initValue.extract(properties));
    }
}
