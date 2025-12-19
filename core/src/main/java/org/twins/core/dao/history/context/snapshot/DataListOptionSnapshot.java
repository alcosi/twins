package org.twins.core.dao.history.context.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.service.i18n.I18nService;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class DataListOptionSnapshot {
    private UUID id;
    private String option;
    private String optionI18n;
    private UUID dataListId;

    public static DataListOptionSnapshot convertEntity(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        if (dataListOptionEntity == null)
            return null;
        return new DataListOptionSnapshot()
                .setId(dataListOptionEntity.getId())
                .setOption(dataListOptionEntity.getOption())
                .setOptionI18n(i18nService.translateToLocale(dataListOptionEntity.getOptionI18nId()))
                .setDataListId(dataListOptionEntity.getDataListId());
    }

    @JsonIgnore
    public String getValue() {
        if (StringUtils.isNotEmpty(optionI18n))
            return optionI18n;
        return option;
    }

    public static void extractTemplateVars(HashMap<String, String> vars, DataListOptionSnapshot dataListOptionSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", dataListOptionSnapshot != null ? dataListOptionSnapshot.id.toString() : "");
        vars.put(prefix + "option", dataListOptionSnapshot != null ? dataListOptionSnapshot.option : "");
        vars.put(prefix + "optionI18n", dataListOptionSnapshot != null ? dataListOptionSnapshot.optionI18n : "");
        vars.put(prefix + "dataList.id", dataListOptionSnapshot != null && dataListOptionSnapshot.dataListId != null ? dataListOptionSnapshot.dataListId.toString() : "");
    }
}