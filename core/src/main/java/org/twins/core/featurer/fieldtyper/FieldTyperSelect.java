package org.twins.core.featurer.fieldtyper;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.fieldtyper.value.FieldValueSelect;
import org.twins.core.service.datalist.DataListOptionService;

import java.util.Properties;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Featurer(id = FeaturerTwins.ID_1305,
        name = "Select",
        description = "")
@Slf4j
public class FieldTyperSelect extends FieldTyperList {
    @FeaturerParam(name = "Multiple", description = "If true, then multiple select available", order = 2)
    public static final FeaturerParamBoolean multiple = new FeaturerParamBoolean("multiple");

    @FeaturerParam(name = "Support custom", description = "If true, then user can enter custom value", order = 3)
    public static final FeaturerParamBoolean supportCustom = new FeaturerParamBoolean("supportCustom");

    @FeaturerParam(name = "Long list threshold", description = "If options count is bigger then given threshold longList type will be used", order = 4)
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @FeaturerParam(name = "Default option id", description = "If has id, will fill the field if nothing else were set", optional = true, order = 5)
    public static final FeaturerParamUUID defaultOptionId = new FeaturerParamUUID("defaultOptionId");

    @Lazy
    private final DataListOptionService dataListOptionService;

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        // TODO add transactional support
        // TODO maybe need to get BAiD at apiUser
        UUID datalistId = dataListId.extract(properties);
        dataListOptionService.processExternalOptions(datalistId, value.getOptions(), twin.getOwnerBusinessAccountId());
        boolean supportCustomValue = supportCustom.extract(properties);
        if (supportCustomValue)
            value.setOptions(dataListService.processNewOptions(datalistId, value.getOptions(), twin.getOwnerBusinessAccountId()));
        else
            value.getOptions().removeIf(o -> ObjectUtils.isEmpty(o.getId()));


        UUID defaultOption = defaultOptionId.extract(properties);

        if (!value.isFilled() && defaultOption != null) {
            value.getOptions().add(dataListOptionService.findEntitySafe(defaultOption));
        }

        super.serializeValue(properties, twin, value, twinChangesCollector);
    }

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        FieldDescriptorList fieldDescriptorList = (FieldDescriptorList) super.getFieldDescriptor(twinClassFieldEntity, properties);
        fieldDescriptorList
                .supportCustom(supportCustom.extract(properties))
                .multiple(multiple.extract(properties))
                .defaultDataListOptionId(defaultOptionId.extract(properties));
        UUID listId = dataListId.extract(properties);
        int listSize = dataListService.countByDataListId(listId);
        if (listSize < longListThreshold.extract(properties)) {
            fieldDescriptorList.options(dataListService.findByDataListId(listId));
        }
        return fieldDescriptorList;
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return multiple.extract(properties);
    }
}
