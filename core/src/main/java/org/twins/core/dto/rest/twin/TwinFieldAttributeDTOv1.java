package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldAttributeV1")
public class TwinFieldAttributeDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin class field attribute id")
    public UUID twinClassFieldAttributeId;

    @Schema(description = "note msg")
    public String noteMsg;

    @Schema(description = "changed at")
    public LocalDateTime changedAt;

    @Schema(description = "context")
    public HashMap<String, String> context;
}
