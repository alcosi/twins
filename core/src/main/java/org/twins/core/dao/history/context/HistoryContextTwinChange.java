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
    private UUID fromTwinId;
    private TwinDraft fromTwin; //in case if twin is already deleted from DB we can display this draft data
    private UUID toTwinId;
    private TwinDraft toTwin; //in case if twin is already deleted from DB we can display this draft data

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("fromTwin.id", fromTwinId != null ? fromTwinId.toString() : "");
        vars.put("fromTwin.name", fromTwin != null ? fromTwin.name : "");
        vars.put("fromTwin.alias", fromTwin != null ? fromTwin.alias : "");
        vars.put("toTwin.id", toTwinId != null ? toTwinId.toString() : "");
        vars.put("toTwin.name", toTwin != null ? toTwin.name : "");
        vars.put("toTwin.alias", toTwin != null ? toTwin.alias : "");
        return vars;
    }

    @Data
    @Accessors(chain = true)
    public static final class TwinDraft {
        private String name;
        private String alias;

        public static TwinDraft convertEntity(TwinEntity twinEntity) {
            if (twinEntity == null)
                return null;
            return new TwinDraft()
                    .setName(twinEntity.getName())
                    .setAlias("TWIN-108"); //todo fix it
        }
    }
}
