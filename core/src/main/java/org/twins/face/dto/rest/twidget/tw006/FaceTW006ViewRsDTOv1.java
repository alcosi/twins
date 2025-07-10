package org.twins.face.dto.rest.twidget.tw006;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FaceTW006ViewRsV1")
public class FaceTW006ViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "twinPointerId")
    public UUID twinPointerId;
}
