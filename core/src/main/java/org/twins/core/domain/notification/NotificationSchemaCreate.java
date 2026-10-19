package org.twins.core.domain.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class NotificationSchemaCreate extends NotificationSchemaSave {
}
