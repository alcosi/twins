package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowFactoryUpdatesV1")
public class TwinflowFactoryUpdateRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "results - twinflow factories")
    public List<TwinflowFactoryDTOv1> twinflowFactories;
}
