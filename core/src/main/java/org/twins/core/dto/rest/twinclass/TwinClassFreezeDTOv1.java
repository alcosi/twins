package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFreezeV1")
public class TwinClassFreezeDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_FREEZE_ID)
    public UUID id;

    @Schema(description = "key")
    public String key;

    @Schema(description = "statusId")
    public UUID statusId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;
}
