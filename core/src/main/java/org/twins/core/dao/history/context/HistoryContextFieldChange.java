package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.FieldSnapshot;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;


@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class HistoryContextFieldChange extends HistoryContext {
    private FieldSnapshot field; //in case if field is already deleted from DB we can display this draft data

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        FieldSnapshot.extractTemplateVars(vars, field, "field");
        String fromValue = getTemplateFromValue();
        String toValue = getTemplateToValue();
        vars.put("fromValue", fromValue != null ? fromValue : "");
        vars.put("toValue", toValue != null ? toValue : "");
        return vars;
    }

    public abstract String getTemplateFromValue();
    public abstract String getTemplateToValue();

    public HistoryContextFieldChange shotField(TwinClassFieldEntity fieldEntity, I18nService i18nService) {
        field = FieldSnapshot.convertEntity(fieldEntity, i18nService);
        return this;
    }


}
