package org.twins.face.dto.rest.bc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "FaceBC001ItemV1", description = "BC001 item config")
public class FaceBC001ItemDTOv1 {

    @Schema(description = "id")
    private UUID id;

    @Schema(description = "order")
    private int order;

    @Schema(description = "twin id")
    private UUID twinId;

    @Schema(description = "label")
    private String label;

    @Schema(description = "icon url")
    private String iconUrl;
}
