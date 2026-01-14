package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.springframework.stereotype.Component;
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

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, FieldValueSelect value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        // TODO add transactional support
        // TODO maybe need to get BAiD at apiUser
        UUID datalistId = dataListId.extract(properties);
        boolean supportCustomValue = supportCustom.extract(properties);
        dataListOptionService.processIncompleteOptions(datalistId, value.getOptions(), twin.getOwnerBusinessAccountId(), supportCustomValue);
        super.serializeValue(properties, twin, value, twinChangesCollector);
    }

    @Override
    public FieldDescriptor getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) throws ServiceException {
        FieldDescriptorList fieldDescriptorList = (FieldDescriptorList) super.getFieldDescriptor(twinClassFieldEntity, properties);
        fieldDescriptorList
                .supportCustom(supportCustom.extract(properties))
                .multiple(multiple.extract(properties));
        UUID listId = dataListId.extract(properties);
        var longListThresholdValue = longListThreshold.extract(properties);
        if (longListThresholdValue > 0) {
            if (longListThresholdValue > 100) {
                log.warn("{}: long list threshold value is too big [{}]. 100 will be used instead.", twinClassFieldEntity.logShort(), longListThresholdValue);
                longListThresholdValue = 100;
            }
            var listSize = dataListService.countByDataListId(listId);
            if (listSize < longListThresholdValue) {
                fieldDescriptorList.options(dataListService.findByDataListId(listId));
            }
        }
        return fieldDescriptorList;
    }

    @Override
    protected boolean allowMultiply(Properties properties) {
        return multiple.extract(properties);
    }
}
