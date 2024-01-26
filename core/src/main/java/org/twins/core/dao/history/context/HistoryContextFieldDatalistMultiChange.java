package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;

import java.util.HashMap;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldDatalistMultiChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.datalistMulti";
    private List<HistoryContextFieldDatalistChange.DataListOptionSnapshot> addedDataListOptionDraftList;
    private List<HistoryContextFieldDatalistChange.DataListOptionSnapshot> deletedDataListOptionDraftList;
    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        //todo add other var in loop
        return vars;
    }

    public HistoryContextFieldDatalistMultiChange shotAddedDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        addedDataListOptionDraftList = CollectionUtils.safeAdd(addedDataListOptionDraftList, HistoryContextFieldDatalistChange.DataListOptionSnapshot
                .convertEntity(dataListOptionEntity, i18nService));
        return this;
    }

    public HistoryContextFieldDatalistMultiChange shotDeletedDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        deletedDataListOptionDraftList = CollectionUtils.safeAdd(deletedDataListOptionDraftList, HistoryContextFieldDatalistChange.DataListOptionSnapshot
                .convertEntity(dataListOptionEntity, i18nService));
        return this;
    }

}
