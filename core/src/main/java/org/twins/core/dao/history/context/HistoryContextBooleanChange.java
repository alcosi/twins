package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.history.context.snapshot.BooleanSnapshot;

import java.util.HashMap;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextBooleanChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.booleanChange";
    private BooleanSnapshot fromBoolean;
    private BooleanSnapshot toBoolean;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextBooleanChange shotFrom(boolean value) {
        this.fromBoolean = BooleanSnapshot.convertEntity(value);
        return this;
    }

    public HistoryContextBooleanChange shotTo(boolean value) {
        this.toBoolean = BooleanSnapshot.convertEntity(value);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        BooleanSnapshot.extractTemplateVars(vars, fromBoolean, "fromBoolean");
        BooleanSnapshot.extractTemplateVars(vars, toBoolean, "toBoolean");
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromBoolean != null ? String.valueOf(fromBoolean.isValue()) : "";
    }

    @Override
    public String templateToValue() {
        return toBoolean != null ? String.valueOf(fromBoolean.isValue()) : "";
    }
}
