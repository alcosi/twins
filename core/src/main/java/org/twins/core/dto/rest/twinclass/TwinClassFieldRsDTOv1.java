package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldRsV1")
public class TwinClassFieldRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - twin class fields list")
    public TwinClassFieldDTOv1 field;
}
