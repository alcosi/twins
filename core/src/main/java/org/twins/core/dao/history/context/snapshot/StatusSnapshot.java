package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class StatusSnapshot {
    private UUID id;
    private String key;
    private String name;
    private String backgroundColor;
    private String fontColor;

    public static StatusSnapshot convertEntity(TwinStatusEntity statusEntity, I18nService i18nService) {
        if (statusEntity == null)
            return null;
        return new StatusSnapshot()
                .setId(statusEntity.getId())
                .setName(i18nService.translateToLocale(statusEntity.getNameI18nId()))
                .setKey(statusEntity.getKey())
                .setBackgroundColor(statusEntity.getBackgroundColor())
                .setFontColor(statusEntity.getFontColor());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, StatusSnapshot statusSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", statusSnapshot != null ? statusSnapshot.id.toString() : "");
        vars.put(prefix + "name", statusSnapshot != null ? statusSnapshot.name : "");
        vars.put(prefix + "backgroundColor", statusSnapshot != null ? statusSnapshot.backgroundColor : "");
        vars.put(prefix + "fontColor", statusSnapshot != null ? statusSnapshot.fontColor : "");
    }
}