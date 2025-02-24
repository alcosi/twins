package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryMultiplierUpdateV1")
public class FactoryMultiplierUpdateDTOv1 extends FactoryMultiplierSaveDTOv1 {
}
