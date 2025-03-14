package org.twins.core.dto.rest.face.page.pg001;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FacePG001ViewRsV1")
public class FacePG001ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - page details")
    public FacePG001DTOv1 page;
}
