package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.StatusSnapshot;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.service.history.HistoryMutableDataCollector;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class HistoryContextStatusChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.statusChange";
    private StatusSnapshot fromStatus; //in case if status is already deleted old DB we can display this draft data
    private StatusSnapshot toStatus; //in case if status is already deleted old DB we can display this draft data
    public static final String PLACEHOLDER_FROM_STATUS = "fromStatus";
    public static final String PLACEHOLDER_TO_STATUS = "toStatus";

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        StatusSnapshot.extractTemplateVars(vars, fromStatus , PLACEHOLDER_FROM_STATUS);
        StatusSnapshot.extractTemplateVars(vars, toStatus , PLACEHOLDER_TO_STATUS);
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromStatus != null ? fromStatus.getName() : "";
    }

    @Override
    public String templateToValue() {
        return toStatus != null ? toStatus.getName() : "";
    }

    public HistoryContextStatusChange shotFromStatus(TwinStatusEntity statusEntity, I18nService i18nService) {
        fromStatus = StatusSnapshot.convertEntity(statusEntity, i18nService);
        return this;
    }

    public HistoryContextStatusChange shotToStatus(TwinStatusEntity statusEntity, I18nService i18nService) {
        toStatus = StatusSnapshot.convertEntity(statusEntity, i18nService);
        return this;
    }

    @Override
    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        boolean hasMutableData = false;
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_FROM_STATUS) && fromStatus != null) {
            mutableDataCollector.getStatusIdSet().add(fromStatus.getId());
            hasMutableData = true;
        }
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_TO_STATUS) && toStatus != null) {
            mutableDataCollector.getStatusIdSet().add(toStatus.getId());
            hasMutableData = true;
        }
        return super.collectMutableData(messageTemplate, mutableDataCollector) || hasMutableData;
    }

    @Override
    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {
        super.spoofSnapshots(mutableDataCollector);
        if (fromStatus != null && mutableDataCollector.getStatusKit().getMap().containsKey(fromStatus.getId()))
            fromStatus = StatusSnapshot.convertEntity(mutableDataCollector.getStatusKit().get(fromStatus.getId()), mutableDataCollector.getI18nService());
        if (toStatus != null && mutableDataCollector.getStatusKit().getMap().containsKey(toStatus.getId()))
            toStatus = StatusSnapshot.convertEntity(mutableDataCollector.getStatusKit().get(toStatus.getId()), mutableDataCollector.getI18nService());
    }
}
