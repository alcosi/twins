package org.twins.core.featurer.fieldrule.fieldoverwriter;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.cambium.featurer.params.FeaturerParamUUIDSet;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldRuleEntity;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorList;
import org.twins.core.featurer.params.FeaturerParamUUIDSetDatalistOptionId;
import org.twins.core.featurer.params.FeaturerParamUUIDSetDatalistSubsetId;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsDataListId;
import org.twins.core.service.datalist.DataListService;

import java.util.Properties;
import java.util.UUID;

@Component
@Featurer(id = FeaturerTwins.ID_4602,
        name = "Field Overwriter - Select",
        description = "Overwrite a field with another datalist or options")
public class FieldParamOverwriterSelect extends FieldParamOverwriter<FieldDescriptorList> {
    @FeaturerParam(name = "Multiple", description = "If true, then multiple select available", order = 2, optional = true)
    public static final FeaturerParamBoolean multiple = new FeaturerParamBoolean("multiple");

    @FeaturerParam(name = "Support custom", description = "If true, then user can enter custom value", order = 3, optional = true)
    public static final FeaturerParamBoolean supportCustom = new FeaturerParamBoolean("supportCustom");

    @FeaturerParam(name = "Long list threshold", description = "If options count is bigger then given threshold longList type will be used", order = 4, optional = true, defaultValue = "0")
    public static final FeaturerParamInt longListThreshold = new FeaturerParamInt("longListThreshold");

    @FeaturerParam(name = "Datalist", description = "", order = 1)
    public static final FeaturerParamUUID listUUID = new FeaturerParamUUIDTwinsDataListId("listUUID");

    @FeaturerParam(name = "datalist option ids", description = "", order = 6, optional = true)
    public static final FeaturerParamUUIDSet dataListOptionIds = new FeaturerParamUUIDSetDatalistOptionId("dataListOptionIds");

    @FeaturerParam(name = "datalist option exclude ids", description = "", order = 7, optional = true)
    public static final FeaturerParamUUIDSet dataListOptionExcludeIds = new FeaturerParamUUIDSetDatalistOptionId("dataListOptionExcludeIds");

    @FeaturerParam(name = "datalist subset ids", description = "", order = 8, optional = true)
    public static final FeaturerParamUUIDSet dataListSubsetIds = new FeaturerParamUUIDSetDatalistSubsetId("dataListSubsetIds");

    @FeaturerParam(name = "datalist subset exclude ids", description = "", order = 9, optional = true)
    public static final FeaturerParamUUIDSet dataListSubsetIdExcludeIds = new FeaturerParamUUIDSetDatalistSubsetId("dataListSubsetIdExcludeIds");

    @Autowired
    @Lazy
    DataListService dataListService;

    @Override
    protected FieldDescriptorList getFieldOverwriterDescriptor(TwinClassFieldRuleEntity twinClassFieldRuleEntity, Properties properties) throws ServiceException {
        UUID listId = listUUID.extract(properties);
        dataListService.checkId(listId, EntitySmartService.CheckMode.NOT_EMPTY_AND_DB_EXISTS);
        int listSize = dataListService.countByDataListId(listId);
        FieldDescriptorList fieldOverwriterDescriptorSelect = new FieldDescriptorList()
                .supportCustom(supportCustom.extract(properties))
                .multiple(multiple.extract(properties));
        if (listSize > longListThreshold.extract(properties))
            fieldOverwriterDescriptorSelect.dataListId(listId);
        else {
            fieldOverwriterDescriptorSelect.options(dataListService.findByDataListId(listId));
        }

        fieldOverwriterDescriptorSelect.applyUUIDSetIfNotEmpty(dataListOptionIds.extract(properties), fieldOverwriterDescriptorSelect::dataListOptionIdList);
        fieldOverwriterDescriptorSelect.applyUUIDSetIfNotEmpty(dataListOptionExcludeIds.extract(properties), fieldOverwriterDescriptorSelect::dataListOptionIdExcludeList);
        fieldOverwriterDescriptorSelect.applyUUIDSetIfNotEmpty(dataListSubsetIds.extract(properties), fieldOverwriterDescriptorSelect::dataListSubsetIdList);
        fieldOverwriterDescriptorSelect.applyUUIDSetIfNotEmpty(dataListSubsetIdExcludeIds.extract(properties), fieldOverwriterDescriptorSelect::dataListSubsetIdExcludeList);

        return fieldOverwriterDescriptorSelect;
    }


}
