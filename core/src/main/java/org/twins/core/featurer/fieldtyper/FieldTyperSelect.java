package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.service.EntitySmartService;

import java.util.*;

@Component
@Featurer(id = 1305,
        name = "FieldTyperSelect",
        description = "")
@Slf4j
public class FieldTyperSelect extends FieldTyperList {

    @FeaturerParam(name = "multiple", description = "If true, then multiple select available")
    public static final FeaturerParamBoolean multiple = new FeaturerParamBoolean("multiple");

    @FeaturerParam(name = "supportCustom", description = "If true, then user can enter custom value")
    public static final FeaturerParamBoolean supportCustom = new FeaturerParamBoolean("supportCustom");

    @FeaturerParam(name = "longListThreshold", description = "If options count is bigger then given threshold longList type will be used")
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @Autowired
    public FieldTyperSelect(DataListOptionRepository dataListOptionRepository, EntitySmartService entitySmartService) {
        super(dataListOptionRepository, entitySmartService);
    }

    @Override
    public FieldDescriptor getFieldDescriptor(Properties properties) {
        UUID listId = listUUID.extract(properties);
        int listSize = dataListOptionRepository.countByDataListId(listId);
        FieldDescriptorList fieldDescriptorList = new FieldDescriptorList()
                .supportCustom(supportCustom.extract(properties))
                .multiple(multiple.extract(properties));
        if (listSize > longListThreshold.extract(properties))
            fieldDescriptorList.dataListId(listId);
        else {
            fieldDescriptorList.options(dataListOptionRepository.findByDataListId(listId));
        }
        return fieldDescriptorList;
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return multiple.extract(properties);
    }
}
