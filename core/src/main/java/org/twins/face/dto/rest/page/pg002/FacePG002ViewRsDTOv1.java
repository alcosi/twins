package org.twins.face.dto.rest.page.pg002;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FacePG002ViewRsV1")
public class FacePG002ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - page details")
    public FacePG002DTOv1 page;
}
