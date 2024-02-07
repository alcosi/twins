package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.LinkSnapshot;
import org.twins.core.dao.link.LinkEntity;

import java.util.HashMap;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextLinkChange extends HistoryContextTwinChange implements IHistoryContextLink {
    public static final String DISCRIMINATOR = "history.linkChange";
    private UUID twinLinkId;
    private LinkSnapshot link; //in case if link is already deleted from DB we can display this draft data

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextLinkChange shotLink(LinkEntity linkEntity, boolean forward, I18nService i18nService) {
        link = LinkSnapshot.convertEntity(linkEntity, forward, i18nService);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = super.extractTemplateVars();
        vars.put("twinLink.id", twinLinkId != null ? twinLinkId.toString() : "");
        LinkSnapshot.extractTemplateVars(vars, link, "link");
        return vars;
    }
}
