package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseCountDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinValidatorCountRsV1")
public class TwinValidatorCountRsDTOv1 extends ResponseCountDTOv1 {
    @Schema(description = "count results grouped by requested fields")
    public List<TwinValidatorCountDTOv1> counts;
}
