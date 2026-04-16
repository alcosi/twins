package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ActionRestrictionReasonV1")
public class ActionRestrictionReasonDTOv1 {
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "type")
    public String type;

    @Schema(description = "description")
    public String description;
}
