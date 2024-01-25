package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryContextTwinChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.twinChange";
    private TwinDraft fromTwin; //in case if twin is already deleted from DB we can display this draft data
    private TwinDraft toTwin; //in case if twin is already deleted from DB we can display this draft data

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextTwinChange shotFromTwin(TwinEntity twinEntity) {
        fromTwin = TwinDraft.convertEntity(twinEntity);
        return this;
    }

    public HistoryContextTwinChange shotToTwin(TwinEntity twinEntity) {
        toTwin = TwinDraft.convertEntity(twinEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("fromTwin.id", fromTwin != null ? fromTwin.id.toString() : "");
        vars.put("fromTwin.name", fromTwin != null ? fromTwin.name : "");
        vars.put("fromTwin.alias", fromTwin != null ? fromTwin.alias : "");
        vars.put("toTwin.id", toTwin != null ? toTwin.id.toString() : "");
        vars.put("toTwin.name", toTwin != null ? toTwin.name : "");
        vars.put("toTwin.alias", toTwin != null ? toTwin.alias : "");
        return vars;
    }

    @Data
    @Accessors(chain = true)
    public static final class TwinDraft {
        private UUID id;
        private String name;
        private String alias;

        public static TwinDraft convertEntity(TwinEntity twinEntity) {
            if (twinEntity == null)
                return null;
            return new TwinDraft()
                    .setId(twinEntity.getId())
                    .setName(twinEntity.getName())
                    .setAlias("TWIN-108"); //todo fix it
        }
    }
}
