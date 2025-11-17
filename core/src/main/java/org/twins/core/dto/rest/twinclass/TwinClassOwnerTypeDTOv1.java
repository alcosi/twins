package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.twinclass.OwnerType;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassOwnerTypeV1")
public class TwinClassOwnerTypeDTOv1 {
    @Schema(description = "owner type", example = DTOExamples.TWIN_CLASS_OWNER_TYPE)
    public OwnerType id;

    @Schema(description = "name", example = DTOExamples.NAME)
    public String name;

    @Schema(description = "description", example = DTOExamples.DESCRIPTION)
    public String description;
}
