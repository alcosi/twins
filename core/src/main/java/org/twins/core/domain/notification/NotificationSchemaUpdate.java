package org.twins.core.domain.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class NotificationSchemaUpdate extends NotificationSchemaSave {
    private UUID id;
}
