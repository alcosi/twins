package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.StatusSnapshot;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.HashMap;

@Data
@Accessors(chain = true)
public class HistoryContextStatusChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.statusChange";
    private StatusSnapshot fromStatus; //in case if status is already deleted old DB we can display this draft data
    private StatusSnapshot toStatus; //in case if status is already deleted old DB we can display this draft data

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
        StatusSnapshot.extractTemplateVars(vars, fromStatus , "fromStatus");
        StatusSnapshot.extractTemplateVars(vars, toStatus , "toStatus");
        return vars;
    }

    public HistoryContextStatusChange shotFromStatus(TwinStatusEntity statusEntity, I18nService i18nService) {
        fromStatus = StatusSnapshot.convertEntity(statusEntity, i18nService);
        return this;
    }

    public HistoryContextStatusChange shotToStatus(TwinStatusEntity statusEntity, I18nService i18nService) {
        toStatus = StatusSnapshot.convertEntity(statusEntity, i18nService);
        return this;
    }
}
