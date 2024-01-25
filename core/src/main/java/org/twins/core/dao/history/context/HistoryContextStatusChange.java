package org.twins.core.dao.history.context;

import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.i18n.service.I18nService;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class HistoryContextStatusChange extends HistoryContext {
    public static final String DISCRIMINATOR = "history.statusChange";
    private StatusSnapshot fromStatus; //in case if status is already deleted old DB we can display this draft data
    private StatusSnapshot toStatus; //in case if status is already deleted old DB we can display this draft data

    @Override
    public String getType() {
        return DISCRIMINATOR;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    protected HashMap<String, String> extractTemplateVars() {
        HashMap<String, String> vars = new HashMap<>();
        vars.put("fromStatus.id", fromStatus != null ? fromStatus.id.toString() : "");
        vars.put("fromStatus.name", fromStatus != null ? fromStatus.name : "");
        vars.put("fromStatus.color", fromStatus != null ? fromStatus.color : "");
        vars.put("toStatus.id", toStatus != null ? toStatus.id.toString() : "");
        vars.put("toStatus.name", toStatus != null ? toStatus.name : "");
        vars.put("toStatus.color", toStatus != null ? toStatus.color : "");
        return vars;
    }

    public HistoryContextStatusChange shotFromStatus(TwinStatusEntity statusEntity, I18nService i18nService) {
        fromStatus = StatusSnapshot.convertEntity(statusEntity, i18nService);
        return this;
    }

    public HistoryContextStatusChange shotToStatus(TwinStatusEntity statusEntity, I18nService i18nService) {
        toStatus = StatusSnapshot.convertEntity(statusEntity, i18nService);
        return this;
    }

    @Data
    @Accessors(chain = true)
    public static final class StatusSnapshot {
        private UUID id;
        private String key;
        private String name;
        private String color;

        public static StatusSnapshot convertEntity(TwinStatusEntity statusEntity, I18nService i18nService) {
            if (statusEntity == null)
                return null;
            return new StatusSnapshot()
                    .setId(statusEntity.getId())
                    .setName(i18nService.translateToLocale(statusEntity.getNameI18nId()))
                    .setKey(statusEntity.getKey())
                    .setColor(statusEntity.getColor());
        }
    }
}
