package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "NotificationSchemaUpdateRqV1")
public class NotificationSchemaUpdateRqDTOv1 extends Request {
    @Schema(description = "notification schema list")
    public List<NotificationSchemaUpdateDTOv1> notificationSchemas;
}
