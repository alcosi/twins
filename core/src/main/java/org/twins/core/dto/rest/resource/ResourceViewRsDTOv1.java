package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ResourceViewRsV1")
public class ResourceViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - resource")
    public ResourceDTOv1 resource;
}
