package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class FieldSnapshot {
    private UUID id;
    private String key;
    private String name;

    public static FieldSnapshot convertEntity(TwinClassFieldEntity fieldEntity, I18nService i18nService) {
        if (fieldEntity == null)
            return null;
        return new FieldSnapshot()
                .setId(fieldEntity.getId())
                .setName(i18nService.translateToLocale(fieldEntity.getNameI18nId()))
                .setKey(fieldEntity.getKey());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, FieldSnapshot fieldSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", fieldSnapshot != null ? fieldSnapshot.id.toString() : "");
        vars.put(prefix + "name", fieldSnapshot != null ? fieldSnapshot.name : "");
        vars.put(prefix + "key", fieldSnapshot != null ? fieldSnapshot.key : "");
    }
}
