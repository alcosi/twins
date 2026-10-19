package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.FactoryConditionGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FactoryConditionCountRqV1")
public class FactoryConditionCountRqDTOv1 extends Request {
    @Schema(description = "search params")
    public FactoryConditionSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields. Empty set = total count without grouping")
    public Set<FactoryConditionGroupField> groupFields;
}
