package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.TwinSnapshot;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.history.HistoryMutableDataCollector;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class HistoryContextTwinChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.twinChange";
    private TwinSnapshot fromTwin; //in case if twin is already deleted from DB we can display this draft data
    private TwinSnapshot toTwin; //in case if twin is already deleted from DB we can display this draft data
    public static final String PLACEHOLDER_FROM_TWIN = "fromTwin";
    public static final String PLACEHOLDER_TO_TWIN = "toTwin";

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextTwinChange shotFromTwin(TwinEntity twinEntity) {
        fromTwin = TwinSnapshot.convertEntity(twinEntity);
        return this;
    }

    public HistoryContextTwinChange shotToTwin(TwinEntity twinEntity) {
        toTwin = TwinSnapshot.convertEntity(twinEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        TwinSnapshot.extractTemplateVars(vars, fromTwin, PLACEHOLDER_FROM_TWIN);
        TwinSnapshot.extractTemplateVars(vars, toTwin, PLACEHOLDER_TO_TWIN);
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromTwin != null ? fromTwin.getName() : "";
    }

    @Override
    public String templateToValue() {
        return toTwin != null ? toTwin.getName() : "";
    }

    @Override
    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        boolean hasMutableData = false;
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_FROM_TWIN) && fromTwin != null) {
            mutableDataCollector.getTwinIdSet().add(fromTwin.getId());
            hasMutableData = true;
        }
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_TO_TWIN) && toTwin != null) {
            mutableDataCollector.getTwinIdSet().add(toTwin.getId());
            hasMutableData = true;
        }
        return super.collectMutableData(messageTemplate, mutableDataCollector) || hasMutableData;
    }

    @Override
    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {
        super.spoofSnapshots(mutableDataCollector);
        if (fromTwin != null && mutableDataCollector.getTwinKit().getMap().containsKey(fromTwin.getId()))
            fromTwin = TwinSnapshot.convertEntity(mutableDataCollector.getTwinKit().get(fromTwin.getId()));
        if (toTwin != null && mutableDataCollector.getTwinKit().getMap().containsKey(toTwin.getId()))
            toTwin = TwinSnapshot.convertEntity(mutableDataCollector.getTwinKit().get(toTwin.getId()));
    }
}
