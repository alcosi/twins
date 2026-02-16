package org.twins.core.domain.notification;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.notification.HistoryNotificationSchemaMapEntity;

@Data
@Accessors(chain = true)
public class HistoryNotificationSchemaMapSave {
    public HistoryNotificationSchemaMapEntity historyNotificationSchemaMap;
}
