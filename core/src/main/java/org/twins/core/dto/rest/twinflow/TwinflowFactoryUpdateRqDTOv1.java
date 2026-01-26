package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowFactoryUpdateRqV1")
public class TwinflowFactoryUpdateRqDTOv1 extends Request {

    @Schema(description = "Twinflow factory list")
    private List<TwinflowFactoryUpdateDTOv1> twinflowFactories;
}
