package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.enums.factory.FactoryLauncher;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowFactoryV1")
public class TwinflowFactoryDTOv1 {

    @Schema(example = DTOExamples.TWINFLOW_FACTORY_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWINFLOW_ID)
    @RelatedObject(type = TwinflowBaseDTOv1.class, name = "twinflow")
    public UUID twinflowId;

    @Schema(example = DTOExamples.TWIN_FACTORY_LAUNCHER_ID)
    public FactoryLauncher twinFactoryLauncherId;

    @Schema(example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "factory")
    public UUID factoryId;
}
