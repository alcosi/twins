package org.twins.core.domain.notification;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.notification.HistoryNotificationEntity;

@Data
@Accessors(chain = true)
public class HistoryNotificationSave {
    public HistoryNotificationEntity historyNotification;
}
