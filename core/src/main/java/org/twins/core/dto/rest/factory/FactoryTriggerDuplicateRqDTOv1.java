package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "FactoryTriggerDuplicateRqV1")
public class FactoryTriggerDuplicateRqDTOv1 extends Request {
    @Schema(description = "duplicates list")
    public List<FactoryTriggerDuplicateDTOv1> duplicates;
}
