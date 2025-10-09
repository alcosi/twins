package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinValidatorSetBaseV1")
public class TwinValidatorSetBaseDTOv1 {

    @Schema(description = "id")
    public UUID id;

    @Schema(description = "domain id")
    @RelatedObject(type = TwinDTOv2.class, name = "domain")
    public UUID domainId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

}


