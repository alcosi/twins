package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinRsV2")
public class TwinRsDTOv2 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin")
    public TwinDTOv2 twin;
}
