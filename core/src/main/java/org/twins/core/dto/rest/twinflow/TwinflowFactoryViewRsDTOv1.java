package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowFactoryViewRsV1")
public class TwinflowFactoryViewRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - twinflow factory")
    public TwinflowFactoryDTOv1 twinflowFactory;
}
