package org.twins.core.domain.notification;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.notification.HistoryNotificationRecipientEntity;

@Data
@Accessors(chain = true)
public class HistoryNotificationRecipientSave {
    public HistoryNotificationRecipientEntity historyNotificationRecipient;
    public I18nEntity nameI18n;
    public I18nEntity descriptionI18n;
}
