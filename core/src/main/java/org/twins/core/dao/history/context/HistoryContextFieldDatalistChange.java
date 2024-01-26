package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldDatalistChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.datalist";
    private DataListOptionSnapshot fromDataListOption;
    private DataListOptionSnapshot toDataListOption;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        vars.put("fromDataListOption.id", fromDataListOption != null ? fromDataListOption.id.toString() : "");
        vars.put("fromDataListOption.option", fromDataListOption != null ? fromDataListOption.option : "");
        vars.put("fromDataListOption.optionI18n", fromDataListOption != null ? fromDataListOption.optionI18n : "");
        vars.put("fromDataListOption.dataList.id", fromDataListOption != null && fromDataListOption.dataListId != null ? fromDataListOption.dataListId.toString() : "");
        vars.put("toDataListOption.id", toDataListOption != null ? toDataListOption.id.toString() : "");
        vars.put("toDataListOption.option", toDataListOption != null ? toDataListOption.option : "");
        vars.put("toDataListOption.optionI18n", toDataListOption != null ? toDataListOption.optionI18n : "");
        vars.put("toDataListOption.dataList.id", toDataListOption != null && toDataListOption.dataListId != null ? toDataListOption.dataListId.toString() : "");
        return vars;
    }

    public HistoryContextFieldDatalistChange shotFromDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        fromDataListOption = DataListOptionSnapshot.convertEntity(dataListOptionEntity, i18nService);
        return this;
    }

    public HistoryContextFieldDatalistChange shotToDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        toDataListOption = DataListOptionSnapshot.convertEntity(dataListOptionEntity, i18nService);
        return this;
    }

    @Data
    @Accessors(chain = true)
    public static final class DataListOptionSnapshot {
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
                    .setOptionI18n(i18nService.translateToLocale(dataListOptionEntity.getOptionI18NId()))
                    .setDataListId(dataListOptionEntity.getDataListId());
        }
    }
}
