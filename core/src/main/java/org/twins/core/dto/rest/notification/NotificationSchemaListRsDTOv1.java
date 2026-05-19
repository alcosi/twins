package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "NotificationSchemaListRsV1")
public class NotificationSchemaListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "notification schema list")
    public List<NotificationSchemaDTOv1> notificationSchemas;
}
