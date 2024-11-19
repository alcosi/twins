package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.context.snapshot.DataListOptionSnapshot;
import org.twins.core.service.history.HistoryMutableDataCollector;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextDatalistChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.datalistChange";
    private DataListOptionSnapshot fromDataListOption;
    private DataListOptionSnapshot toDataListOption;
    public static final String PLACEHOLDER_FROM_DATALIST_OPTION = "fromDataListOption";
    public static final String PLACEHOLDER_TO_DATALIST_OPTION = "toDataListOption";

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        DataListOptionSnapshot.extractTemplateVars(vars, fromDataListOption, PLACEHOLDER_FROM_DATALIST_OPTION);
        DataListOptionSnapshot.extractTemplateVars(vars, toDataListOption, PLACEHOLDER_TO_DATALIST_OPTION);
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


    @Override
    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        boolean hasMutableData = false;
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_FROM_DATALIST_OPTION) && fromDataListOption != null) {
            mutableDataCollector.getDataListOptionIdSet().add(fromDataListOption.getId());
            hasMutableData = true;
        }
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_TO_DATALIST_OPTION) && toDataListOption != null) {
            mutableDataCollector.getDataListOptionIdSet().add(toDataListOption.getId());
            hasMutableData = true;
        }
        return super.collectMutableData(messageTemplate, mutableDataCollector) || hasMutableData;
    }

    @Override
    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {
        super.spoofSnapshots(mutableDataCollector);
        if (fromDataListOption != null && mutableDataCollector.getDataListOptionKit().getMap().containsKey(fromDataListOption.getId()))
            fromDataListOption = DataListOptionSnapshot.convertEntity(mutableDataCollector.getDataListOptionKit().get(fromDataListOption.getId()), mutableDataCollector.getI18nService());
        if (toDataListOption != null && mutableDataCollector.getDataListOptionKit().getMap().containsKey(toDataListOption.getId()))
            toDataListOption = DataListOptionSnapshot.convertEntity(mutableDataCollector.getDataListOptionKit().get(toDataListOption.getId()), mutableDataCollector.getI18nService());
    }
}
