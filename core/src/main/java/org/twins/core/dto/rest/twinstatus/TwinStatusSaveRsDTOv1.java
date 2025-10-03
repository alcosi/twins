package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStatusSaveRsV1")
public class TwinStatusSaveRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin status")
    public TwinStatusDTOv1 twinStatus;
}
