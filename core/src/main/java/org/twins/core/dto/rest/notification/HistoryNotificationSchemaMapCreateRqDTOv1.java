package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationSchemaMapCreateRequestV1")
public class HistoryNotificationSchemaMapCreateRqDTOv1 extends Request {
    @Schema(description = "history notification schema maps")
    public List<HistoryNotificationSchemaMapCreateDTOv1> historyNotificationSchemaMaps;
}
