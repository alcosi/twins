package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.history.context.snapshot.LinkSnapshot;
import org.twins.core.dao.history.context.snapshot.TwinSnapshot;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.service.history.HistoryMutableDataCollector;

import java.util.HashMap;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryContextLink extends HistoryContext implements IHistoryContextLink {
    public static final String DISCRIMINATOR = "history.link";
    private UUID twinLinkId;
    private LinkSnapshot link; //in case if link is already deleted from DB we can display this draft data
    private TwinSnapshot dstTwin; //in case if twin is already deleted from DB we can display this draft data
    public static final String PLACEHOLDER_LINK = "link";
    public static final String PLACEHOLDER_DST_TWIN = "dstTwin";

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    public HistoryContextLink shotLink(LinkEntity linkEntity, boolean forward, I18nService i18nService) {
        link = LinkSnapshot.convertEntity(linkEntity, forward, i18nService);
        return this;
    }

    public HistoryContextLink shotDstTwin(TwinEntity twinEntity) {
        dstTwin = TwinSnapshot.convertEntity(twinEntity);
        return this;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("twinLink.id", twinLinkId != null ? twinLinkId.toString() : "");
        LinkSnapshot.extractTemplateVars(vars, link, PLACEHOLDER_LINK);
        TwinSnapshot.extractTemplateVars(vars, dstTwin, PLACEHOLDER_DST_TWIN);
        return vars;
    }

    @Override
    public String templateFromValue() {
        return null;  //todo fixme
    }

    @Override
    public String templateToValue() {
        return null;  //todo fixme
    }

    @Override
    public boolean collectMutableData(String messageTemplate, HistoryMutableDataCollector mutableDataCollector) {
        boolean hasMutableData = false;
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_LINK) && link != null) {
            mutableDataCollector.getLinkIdSet().add(link.getId());
            hasMutableData = true;
        }
        if (containPlaceHolder(messageTemplate, PLACEHOLDER_DST_TWIN) && dstTwin != null) {
            mutableDataCollector.getTwinIdSet().add(dstTwin.getId());
            hasMutableData = true;
        }
        return super.collectMutableData(messageTemplate, mutableDataCollector) || hasMutableData;
    }

    @Override
    public void spoofSnapshots(HistoryMutableDataCollector mutableDataCollector) {
        super.spoofSnapshots(mutableDataCollector);
        if (link != null && mutableDataCollector.getLinkKit().getMap().containsKey(link.getId()))
            link = LinkSnapshot.convertEntity(mutableDataCollector.getLinkKit().get(link.getId()), link.isForward(), mutableDataCollector.getI18nService());
        if (dstTwin != null && mutableDataCollector.getTwinKit().getMap().containsKey(dstTwin.getId()))
            dstTwin = TwinSnapshot.convertEntity(mutableDataCollector.getTwinKit().get(dstTwin.getId()));
    }
}
