package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryMultiplierDuplicateRqV1")
public class FactoryMultiplierDuplicateRqDTOv1 extends Request {
    @Schema(description = "duplicates list")
    @Size(min = 1, max = 50)
    public List<FactoryMultiplierDuplicateDTOv1> duplicates;
}
