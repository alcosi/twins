package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldValidatorViewRsV1")
public class TwinClassFieldValidatorViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "twin class field validator")
    public TwinClassFieldValidatorDTOv1 validator;
}
