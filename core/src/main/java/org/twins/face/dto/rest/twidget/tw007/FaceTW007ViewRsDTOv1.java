package org.twins.face.dto.rest.twidget.tw007;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceTW007ViewRsV1")
public class FaceTW007ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - widget details")
    public FaceTW007DTOv1 widget;
}
