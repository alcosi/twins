package org.twins.core.domain.notification;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.notification.NotificationSchemaEntity;
import org.twins.core.dao.i18n.I18nEntity;

@Data
@Accessors(chain = true)
public class NotificationSchemaSave {
    public NotificationSchemaEntity notificationSchema;
    public I18nEntity nameI18n;
    public I18nEntity descriptionI18n;
}
