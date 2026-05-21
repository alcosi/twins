package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "NotificationSchemaUpdateV1")
public class NotificationSchemaUpdateDTOv1 extends NotificationSchemaSaveDTOv1 {
    @Schema(description = "notification schema id", example = DTOExamples.UUID_ID)
    public UUID id;
}
