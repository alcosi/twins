package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.context.snapshot.DataListOptionSnapshot;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextDatalistChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.datalistChange";
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
    public String templateFromValue() {
        return fromDataListOption != null ? fromDataListOption.getValue() : "";
    }

    @Override
    public String templateToValue() {
        return toDataListOption != null ? toDataListOption.getValue() : "";
    }

    public HistoryContextDatalistChange shotFromDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        fromDataListOption = DataListOptionSnapshot.convertEntity(dataListOptionEntity, i18nService);
        return this;
    }

    public HistoryContextDatalistChange shotToDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        toDataListOption = DataListOptionSnapshot.convertEntity(dataListOptionEntity, i18nService);
        return this;
    }
}
