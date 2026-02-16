package org.twins.core.dto.rest.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "HistoryNotificationSchemaMapUpdateV1")
public class HistoryNotificationSchemaMapUpdateDTOv1 extends HistoryNotificationSchemaMapSaveDTOv1 {
    @Schema(description = "id")
    public UUID id;
}
