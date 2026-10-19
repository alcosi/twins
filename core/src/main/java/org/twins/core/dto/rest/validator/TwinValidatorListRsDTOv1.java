package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorListRsV1")
public class TwinValidatorListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "twin validator list")
    public List<TwinValidatorDTOv1> validators;

}
