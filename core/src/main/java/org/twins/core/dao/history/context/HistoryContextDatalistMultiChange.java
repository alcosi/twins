package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.common.util.CollectionUtils;
import org.twins.core.i18n.service.I18nService;
import org.twins.core.dao.datalist.DataListOptionEntity;
import org.twins.core.dao.history.context.snapshot.DataListOptionSnapshot;

import java.util.HashMap;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextDatalistMultiChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.datalistMultiChange";
    private List<DataListOptionSnapshot> addedDataListOptionSnapshotList;
    private List<DataListOptionSnapshot> deletedDataListOptionSnapshotList;
    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        //todo loop lists and add more template vars
        return vars;
    }

    @Override
    public String templateFromValue() {
        return null; //todo any idea?
    }

    @Override
    public String templateToValue() {
        return null; //todo any idea?
    }

    public HistoryContextDatalistMultiChange shotAddedDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        addedDataListOptionSnapshotList = CollectionUtils.safeAdd(addedDataListOptionSnapshotList, DataListOptionSnapshot
                .convertEntity(dataListOptionEntity, i18nService));
        return this;
    }

    public HistoryContextDatalistMultiChange shotDeletedDataListOption(DataListOptionEntity dataListOptionEntity, I18nService i18nService) {
        deletedDataListOptionSnapshotList = CollectionUtils.safeAdd(deletedDataListOptionSnapshotList, DataListOptionSnapshot
                .convertEntity(dataListOptionEntity, i18nService));
        return this;
    }

    public boolean notEmpty() {
        return CollectionUtils.isNotEmpty(addedDataListOptionSnapshotList) || CollectionUtils.isNotEmpty(deletedDataListOptionSnapshotList);
    }
}
