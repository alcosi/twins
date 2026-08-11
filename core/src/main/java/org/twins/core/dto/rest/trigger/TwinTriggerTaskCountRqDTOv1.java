package org.twins.core.dto.rest.trigger;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinTriggerTaskGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinTriggerTaskCountRqV1")
public class TwinTriggerTaskCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinTriggerTaskSearchDTOv1 search;

    @Size(max = 6)
    @Schema(description = "Group by fields")
    public Set<TwinTriggerTaskGroupField> groupFields;
}
