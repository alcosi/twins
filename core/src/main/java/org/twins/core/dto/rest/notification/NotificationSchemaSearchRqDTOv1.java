package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "NotificationSchemaSearchRqV1")
public class NotificationSchemaSearchRqDTOv1 extends Request {
    @Schema(description = "search")
    public NotificationSchemaSearchDTOv1 search;
}
