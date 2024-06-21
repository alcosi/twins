package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.service.EntitySmartService;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_1305,
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

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        boolean supportCustomValue = supportCustom.extract(properties);
        // TODO add transactional support
        // TODO maybe need to get BAiD at apiUser
        if (supportCustomValue)
            value.setOptions(dataListService.processNewOptions(listUUID.extract(properties), value.getOptions(), twin.getOwnerBusinessAccountId()));
        else
            value.getOptions().removeIf(o -> ObjectUtils.isEmpty(o.getId()));
        super.serializeValue(properties, twin, value, twinChangesCollector);
    }

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        UUID listId = listUUID.extract(properties);
        dataListService.checkId(listId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
        int listSize = dataListService.countByDataListId(listId);
        FieldDescriptorList fieldDescriptorList = new FieldDescriptorList()
                .supportCustom(supportCustom.extract(properties))
                .multiple(multiple.extract(properties));
        if (listSize > longListThreshold.extract(properties))
            fieldDescriptorList.dataListId(listId);
        else {
            fieldDescriptorList.options(dataListService.findByDataListId(listId));
        }
        return fieldDescriptorList;
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return multiple.extract(properties);
    }
}
