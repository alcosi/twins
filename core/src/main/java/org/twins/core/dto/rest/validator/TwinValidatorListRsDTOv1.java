package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Schema(name = "TwinValidatorListRsV1")
@Accessors(chain = true)
public class TwinValidatorListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin validators")
    public List<TwinValidatorDTOv1> twinValidators;
}
