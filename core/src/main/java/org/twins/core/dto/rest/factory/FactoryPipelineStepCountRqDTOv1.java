package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.FactoryPipelineStepGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryPipelineStepCountRqV1")
public class FactoryPipelineStepCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public FactoryPipelineStepSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields. Empty set = total count without grouping")
    public Set<FactoryPipelineStepGroupField> groupFields;
}
