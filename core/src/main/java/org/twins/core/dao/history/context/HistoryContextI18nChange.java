package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.dao.I18nEntity;
import org.twins.core.dao.history.context.snapshot.I18nSnapshot;

import java.util.HashMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextI18nChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.i18nChange";
    private I18nSnapshot fromI18n;
    private I18nSnapshot toI18n;

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextI18nChange shotFromI18n(I18nEntity i18nEntity) {
        fromI18n = I18nSnapshot.convertEntity(i18nEntity);
        return this;
    }

    public HistoryContextI18nChange shotToI18n(I18nEntity i18nEntity) {
        toI18n = I18nSnapshot.convertEntity(i18nEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        I18nSnapshot.extractTemplateVars(vars, fromI18n, "fromI18n");
        I18nSnapshot.extractTemplateVars(vars, toI18n, "toI18n");
        return vars;
    }

    @Override
    public String templateFromValue() {
        return fromI18n != null ? fromI18n.getName() : "";
    }

    @Override
    public String templateToValue() {
        return toI18n != null ? toI18n.getName() : "";
    }
}
