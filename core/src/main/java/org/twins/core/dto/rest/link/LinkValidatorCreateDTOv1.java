package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "LinkValidatorCreateV1")
public class LinkValidatorCreateDTOv1 {

    @NotNull
    @Schema(description = "link id this validator belongs to")
    public UUID linkId;

    @NotNull
    @Schema(description = "linker featurer id")
    public Integer linkerFeaturerId;

    @Schema(description = "linker featurer params")
    public HashMap<String, String> linkerParams;

    @Schema(description = "[optional] order within the link; defaults to max existing order + 1")
    public Integer order;
}
