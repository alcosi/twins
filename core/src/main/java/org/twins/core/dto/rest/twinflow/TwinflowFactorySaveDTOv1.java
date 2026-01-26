package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.factory.FactoryLauncher;

import java.util.UUID;


@Data
@Accessors(chain = true)
@Schema(name =  "TwinflowFactorySaveV1")
public class TwinflowFactorySaveDTOv1 {

    @Schema(example = DTOExamples.TWINFLOW_ID)
    public UUID twinflowId;

    @Schema(example = DTOExamples.TWIN_FACTORY_LAUNCHER_ID)
    public FactoryLauncher twinFactoryLauncherId;

    @Schema(example = DTOExamples.FACTORY_ID)
    public UUID factoryId;
}
