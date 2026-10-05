package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldAttributeUpdateV1")
public class TwinFieldAttributeUpdateDTOv1 {

    @NotNull
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin class field attribute id")
    public UUID twinClassFieldAttributeId;

    @Schema(description = "msg")
    public String msg;

    @Schema(description = "context")
    public HashMap<String, String> context;

}
