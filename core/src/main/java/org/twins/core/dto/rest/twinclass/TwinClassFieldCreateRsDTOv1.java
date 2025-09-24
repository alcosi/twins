package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldCreateRsV1")
public class TwinClassFieldCreateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - created twin class field")
    public TwinClassFieldDTOv1 field;
}
