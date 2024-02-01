package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.TwinSnapshot;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class HistoryContextTwinChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.twinChange";
    private TwinSnapshot fromTwin; //in case if twin is already deleted from DB we can display this draft data
    private TwinSnapshot toTwin; //in case if twin is already deleted from DB we can display this draft data

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
        TwinSnapshot.extractTemplateVars(vars, fromTwin, "fromTwin");
        TwinSnapshot.extractTemplateVars(vars, toTwin, "toTwin");
        return vars;
    }
}
