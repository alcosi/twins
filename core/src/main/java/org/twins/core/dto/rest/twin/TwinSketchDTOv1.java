package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinCreateRsV1")
public class TwinSketchDTOv1 {
    @Schema(description = "new twin id")
    public UUID twinId;
}
