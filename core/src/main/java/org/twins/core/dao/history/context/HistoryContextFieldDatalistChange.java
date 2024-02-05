package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.context.snapshot.DataListOptionSnapshot;

import java.util.HashMap;

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
        DataListOptionSnapshot.extractTemplateVars(vars, fromDataListOption, "fromDataListOption");
        DataListOptionSnapshot.extractTemplateVars(vars, toDataListOption, "toDataListOption");
        return vars;
    }

    @Override
    public String getTemplateFromValue() {
        if (fromDataListOption == null)
            return "";
        if (StringUtils.isNotEmpty(fromDataListOption.getOptionI18n()))
            return fromDataListOption.getOptionI18n();
        return fromDataListOption.getOption();
    }

    @Override
    public String getTemplateToValue() {
        return toDataListOption != null ? toDataListOption.getValue() : "";
    }

    public HistoryContextFieldDatalistChange shotFromDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        fromDataListOption = DataListOptionSnapshot.convertEntity(dataListOptionEntity, i18nService);
        return this;
    }

    public HistoryContextFieldDatalistChange shotToDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        toDataListOption = DataListOptionSnapshot.convertEntity(dataListOptionEntity, i18nService);
        return this;
    }
}
