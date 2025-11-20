package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowFactoryUpdateV1")
public class TwinflowFactoryUpdateDTOv1 extends TwinflowFactorySaveDTOv1 {

    @Schema(example = DTOExamples.FACTORY_ID)
    private UUID id;
}
