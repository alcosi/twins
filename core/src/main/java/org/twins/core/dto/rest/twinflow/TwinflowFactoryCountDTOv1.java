package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.factory.FactoryDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.enums.factory.FactoryLauncher;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "TwinflowFactoryCountV1")
public class TwinflowFactoryCountDTOv1 extends CountDTOv1 {
    @Schema(description = "twinflow id", example = DTOExamples.TWINFLOW_ID)
    @RelatedObject(type = TwinflowBaseDTOv1.class, name = "twinflow")
    public UUID twinflowId;

    @Schema(description = "factory id", example = DTOExamples.FACTORY_ID)
    @RelatedObject(type = FactoryDTOv1.class, name = "factory")
    public UUID factoryId;

    @Schema(description = "twin factory launcher id", example = DTOExamples.TWIN_FACTORY_LAUNCHER_ID)
    public FactoryLauncher twinFactoryLauncherId;
}
