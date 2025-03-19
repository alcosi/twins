package org.twins.core.dao.history.context.snapshot;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.StringUtils;
import org.twins.core.dao.i18n.I18nEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class I18nSnapshot {
    private UUID id;
    private String key;
    private String name;

    public static I18nSnapshot convertEntity(I18nEntity i18nEntity) {
        if (i18nEntity == null)
            return null;
        return new I18nSnapshot()
                .setId(i18nEntity.getId())
                .setName(i18nEntity.getName())
                .setKey(i18nEntity.getKey());
    }

    public static void extractTemplateVars(HashMap<String, String> vars, I18nSnapshot i18nSnapshot, String prefix) {
        prefix = StringUtils.isNotEmpty(prefix) ? prefix + "." : "";
        vars.put(prefix + "id", i18nSnapshot != null ? i18nSnapshot.id.toString() : "");
        vars.put(prefix + "name", i18nSnapshot != null ? i18nSnapshot.name : "");
    }
}
