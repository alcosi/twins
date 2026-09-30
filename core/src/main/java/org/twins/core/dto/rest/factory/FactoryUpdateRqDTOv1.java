package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryUpdateRqV1")
public class FactoryUpdateRqDTOv1 extends Request {
    @Valid
    @Schema(description = "factory update list")
    public List<FactoryUpdateDTOv1> factories;
}
