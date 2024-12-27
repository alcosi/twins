package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryMultiplierV2")
public class FactoryMultiplierDTOv2 extends FactoryMultiplierDTOv1 {
    @Schema(description = "factory")
    public FactoryDTOv1 factory;

    @Schema(description = "input twin class")
    public TwinClassBaseDTOv1 inputTwinClass;
}
