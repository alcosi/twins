package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorViewRsV1")
public class TwinValidatorViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "twin validator")
    public TwinValidatorDTOv1 validator;

}
