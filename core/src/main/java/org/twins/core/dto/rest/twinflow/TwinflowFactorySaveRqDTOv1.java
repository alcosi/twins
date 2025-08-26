package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.domain.factory.FactoryLauncher;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowFactorySaveRqV1")
public class TwinflowFactorySaveRqDTOv1 extends Request {

    @Schema(example = DTOExamples.TWINFLOW_ID)
    public UUID twinflowId;

    @Schema(example = DTOExamples.TWIN_FACTORY_LAUNCHER_ID)
    public FactoryLauncher twinFactoryLauncherId;

    @Schema(example = DTOExamples.FACTORY_ID)
    public UUID twinFactoryId;
}
