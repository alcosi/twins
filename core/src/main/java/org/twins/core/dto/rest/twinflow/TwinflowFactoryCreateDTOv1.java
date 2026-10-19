package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.factory.FactoryLauncher;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowFactoryCreateV1")
public class TwinflowFactoryCreateDTOv1 {

    @NotNull
    @Schema(example = DTOExamples.TWINFLOW_ID)
    public UUID twinflowId;

    @NotNull
    @Schema(example = DTOExamples.TWIN_FACTORY_LAUNCHER_ID)
    public FactoryLauncher twinFactoryLauncherId;

    @NotNull
    @Schema(example = DTOExamples.FACTORY_ID)
    public UUID factoryId;
}
