package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.TwinClassSnapshot;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextTwinClassChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.twinClassChange";
    private TwinClassSnapshot fromClass;
    private TwinClassSnapshot toClass;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextTwinClassChange shotFromClass(TwinClassEntity twinClassEntity) {
        fromClass = TwinClassSnapshot.convertEntity(twinClassEntity);
        return this;
    }

    public HistoryContextTwinClassChange shotToClass(TwinClassEntity twinClassEntity) {
        toClass = TwinClassSnapshot.convertEntity(twinClassEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        TwinClassSnapshot.extractTemplateVars(vars, fromClass, "fromClass");
        TwinClassSnapshot.extractTemplateVars(vars, toClass, "toClass");
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromClass != null ? fromClass.getKey() : "";
    }

    @Override
    public String templateToValue() {
        return toClass != null ? toClass.getKey() : "";
    }
}
