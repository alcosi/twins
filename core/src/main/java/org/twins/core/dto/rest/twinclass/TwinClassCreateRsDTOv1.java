package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassCreateRsV1")
public class TwinClassCreateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "result - twin class")
    public TwinClassDTOv1 twinClass;
}
