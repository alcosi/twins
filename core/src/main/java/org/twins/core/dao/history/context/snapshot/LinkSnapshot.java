package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.link.LinkEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class LinkSnapshot {
    private UUID id;
    private String name; // will be different for backward twin anf forward twin history
    private boolean forward;

    public static LinkSnapshot convertEntity(LinkEntity linkEntity, boolean forward, I18nService i18nService) {
        if (linkEntity == null)
            return null;
        return new LinkSnapshot()
                .setId(linkEntity.getId())
                .setName(i18nService.translateToLocale(forward ? linkEntity.getForwardNameI18NId() : linkEntity.getBackwardNameI18NId()))
                .setForward(forward);
    }

    public static void extractTemplateVars(HashMap<String, String> vars, LinkSnapshot linkSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", linkSnapshot != null ? linkSnapshot.id.toString() : "");
        vars.put(prefix + "name", linkSnapshot != null ? linkSnapshot.name : "");
        vars.put(prefix + "forward", linkSnapshot != null ? String.valueOf(linkSnapshot.forward) : "");
    }
}
