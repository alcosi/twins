package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "LinkValidatorUpdateV1")
public class LinkValidatorUpdateDTOv1 {

    @NotNull
    @Schema(description = "id", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "link id this validator belongs to")
    public UUID linkId;

    @Schema(description = "linker featurer id")
    public Integer linkerFeaturerId;

    @Schema(description = "linker featurer params")
    public HashMap<String, String> linkerParams;

    @Schema(description = "order within the link")
    public Integer order;
}
