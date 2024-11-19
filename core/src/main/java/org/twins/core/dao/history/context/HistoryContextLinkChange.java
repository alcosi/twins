package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.LinkSnapshot;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.service.history.HistoryMutableDataCollector;

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
        LinkSnapshot.extractTemplateVars(vars, link, HistoryContextLink.PLACEHOLDER_LINK);
        return vars;
    }

    @Override
    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        boolean hasMutableData = false;
        if (containPlaceHolder(messageTemplate, HistoryContextLink.PLACEHOLDER_LINK) && link != null) {
            mutableDataCollector.getLinkIdSet().add(link.getId());
            hasMutableData = true;
        }
        return super.collectMutableData(messageTemplate, mutableDataCollector) || hasMutableData;
    }

    @Override
    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {
        super.spoofSnapshots(mutableDataCollector);
        if (link != null && mutableDataCollector.getLinkKit().getMap().containsKey(link.getId()))
            link = LinkSnapshot.convertEntity(mutableDataCollector.getLinkKit().get(link.getId()), link.isForward(), mutableDataCollector.getI18nService());
    }
}
