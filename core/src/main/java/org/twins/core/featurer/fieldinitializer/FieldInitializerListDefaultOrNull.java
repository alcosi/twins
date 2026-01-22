package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListEntity;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.FieldTyperList;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListOptionService;
import org.twins.core.service.datalist.DataListService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5305,
        name = "Option initializer from data list default option",
        description = "")
@Slf4j
public class FieldInitializerListDefaultOrNull extends FieldInitializer<FieldDescriptorList, FieldValueSelect> implements FieldInitializerThrowIfNull {
    @Autowired
    @Lazy
    DataListService dataListService;

    @Autowired
    DataListOptionService dataListOptionService;

    @Autowired
    @Lazy
    FeaturerService featurerService;

    @Override
    protected void setInitValue(Properties properties, TwinEntity twin, FieldValueSelect value) throws ServiceException {
        var twinClassField = value.getTwinClassField();
        Properties fieldTyperProperties = featurerService.extractProperties(twinClassField.getFieldTyperFeaturerId(), twinClassField.getFieldTyperParams());
        var dataListId = FieldTyperList.getDataListId(fieldTyperProperties);
        if (dataListId == null) {
            return;
        }
        DataListEntity dataListEntity = dataListService.findEntitySafe(dataListId);
        var defaultOptionIdValue = dataListEntity.getDefaultDataListOptionId();
        if (defaultOptionIdValue != null) {
            DataListOptionEntity defaultOption = dataListOptionService.findEntitySafe(defaultOptionIdValue);
            value.getOptions().clear();
            value.add(defaultOption);
        } else if (throwIfNull.extract(properties)) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, dataListEntity.logNormal() + " has no default option");
        }
    }
}
