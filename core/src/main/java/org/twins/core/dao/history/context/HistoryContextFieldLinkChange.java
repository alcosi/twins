package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.LinkSnapshot;
import org.twins.core.dao.history.context.snapshot.TwinSnapshot;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class HistoryContextFieldLinkChange extends HistoryContextFieldChange {
    public static final String DISCRIMINATOR = "history.fieldChange.link";
    private UUID twinLinkId;
    private LinkSnapshot link; //in case if link is already deleted from DB we can display this draft data
    private TwinSnapshot fromTwin; //in case if twin is already deleted from DB we can display this draft data
    private TwinSnapshot toTwin; //in case if twin is already deleted from DB we can display this draft data

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextFieldLinkChange shotLink(LinkEntity linkEntity, boolean forward, I18nService i18nService) {
        link = LinkSnapshot.convertEntity(linkEntity, forward, i18nService);
        return this;
    }

    public HistoryContextFieldLinkChange shotFromTwin(TwinEntity twinEntity) {
        fromTwin = TwinSnapshot.convertEntity(twinEntity);
        return this;
    }

    public HistoryContextFieldLinkChange shotToTwin(TwinEntity twinEntity) {
        toTwin = TwinSnapshot.convertEntity(twinEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        vars.put("twinLink.id", twinLinkId != null ? twinLinkId.toString() : "");
        TwinSnapshot.extractTemplateVars(vars, fromTwin, "fromTwin");
        TwinSnapshot.extractTemplateVars(vars, toTwin, "toTwin");
        LinkSnapshot.extractTemplateVars(vars, link, "link");
        return vars;
    }

    @Override
    public String getTemplateFromValue() {
        return fromTwin != null ? fromTwin.getName() : "";
    }

    @Override
    public String getTemplateToValue() {
        return toTwin != null ? toTwin.getName() : "";
    }
}
