package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "LinkValidatorDuplicateV1")
public class LinkValidatorDuplicateDTOv1 {
    @Schema(description = "original link validator id")
    public UUID originalLinkValidatorId;

    @Schema(description = "[optional] fill if validator should be copied to other link; omit to duplicate in place (gets a free order)")
    public UUID newLinkId;
}
