package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.HashMap;
import java.util.UUID;


@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class HistoryContextFieldChange extends HistoryContext {
    private FieldSnapshot field; //in case if field is already deleted from DB we can display this draft data

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("field.id",field != null ? field.id.toString() : "");
        vars.put("field.name", field != null ? field.name : "");
        vars.put("field.key", field != null ? field.key : "");
        return vars;
    }

    public HistoryContextFieldChange shotField(TwinClassFieldEntity fieldEntity, I18nService i18nService) {
        field = FieldSnapshot.convertEntity(fieldEntity, i18nService);
        return this;
    }

    @Data
    @Accessors(chain = true)
    public static final class FieldSnapshot {
        private UUID id;
        private String key;
        private String name;

        public static FieldSnapshot convertEntity(TwinClassFieldEntity fieldEntity, I18nService i18nService) {
            if (fieldEntity == null)
                return null;
            return new FieldSnapshot()
                    .setId(fieldEntity.getId())
                    .setName(i18nService.translateToLocale(fieldEntity.getNameI18NId()))
                    .setKey(fieldEntity.getKey());
        }
    }
}
