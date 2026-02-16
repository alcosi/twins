package org.twins.core.domain.notification;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class HistoryNotificationSchemaMapUpdate extends HistoryNotificationSchemaMapSave {
    private UUID id;
}
