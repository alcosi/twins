package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "TwinStatusV1")
public class TwinAliasDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;

    @Schema(description = "alias", example = "TWIN-D41")
    public String alias;

    @Schema(description = "twin id", example = "")
    public UUID twinId;

    @Schema(description = "domain id", example = "")
    public UUID domainId;

    @Schema(description = "business account id", example = "")
    public UUID businessAccountId;

    @Schema(description = "user id", example = "")
    public UUID userId;
}
