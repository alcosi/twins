package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.domain.factory.FactoryLauncher;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryDTOv2;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowFactoryBaseV1")
public class TwinflowFactoryBaseDTOv1 {

    @Schema(example = DTOExamples.TWINFLOW_FACTORY_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWINFLOW_ID)
    public UUID twinflowId;

    @Schema(example = DTOExamples.TWIN_FACTORY_LAUNCHER_ID)
    public FactoryLauncher twinFactoryLauncherId;

    @Schema(example = DTOExamples.FACTORY_ID)
    public UUID twinFactoryId;

    @Schema(name = "twinflow")
    public TwinflowBaseDTOv1 twinflow;

    @Schema(name = "twin factory")
    public FactoryDTOv2 twinFactory;
}
