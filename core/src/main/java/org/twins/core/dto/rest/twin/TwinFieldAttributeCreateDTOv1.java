package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinFieldAttributeCreateV1")
public class TwinFieldAttributeCreateDTOv1 {

    @NotNull
    @Schema(description = "twin class field id")
    public UUID twinClassFieldId;

    @NotNull
    @Schema(description = "twin class field attribute id")
    public UUID twinClassFieldAttributeId;

    @Schema(description = "msg")
    public String msg;

    @Schema(description = "context")
    public HashMap<String, String> context;

}
