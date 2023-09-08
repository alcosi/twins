package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.datalist.DataListOptionRepository;
import org.twins.core.dao.twin.TwinFieldEntity;
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
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        UUID listId = listUUID.extract(properties);
        int listSize = dataListOptionRepository.countByDataListId(listId);
        FieldTypeUIDescriptor fieldTypeUIDescriptor = new FieldTypeUIDescriptor()
                .addParam("supportCustom", supportCustom.extract(properties).toString())
                .addParam("multiple", multiple.extract(properties).toString());
        if (listSize > longListThreshold.extract(properties))
            return fieldTypeUIDescriptor
                    .type("selectLongList")
                    .addParam("listId", listId.toString());
        else {
            List<String> options = new ArrayList<>();
            for (DataListOptionEntity optionEntity : dataListOptionRepository.findByDataListId(listId))
                options.add(optionEntity.option());
            return fieldTypeUIDescriptor
                    .type("select")
                    .addParam("options", options);
        }

    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return multiple.extract(properties);
    }
}
