package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryMultiplierCreateRqV1")
public class FactoryMultiplierCreateRqDTOv1 extends Request {
    @Schema(description = "factory multiplier create")
    public FactoryMultiplierCreateDTOv1 factoryMultiplier;
}
