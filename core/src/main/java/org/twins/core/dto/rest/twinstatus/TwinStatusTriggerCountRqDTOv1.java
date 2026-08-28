package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinStatusTriggerGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinStatusTriggerCountRqV1")
public class TwinStatusTriggerCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinStatusTriggerSearchDTOv1 search;

    @Size(max = 5)
    @Schema(description = "Group by fields")
    public Set<TwinStatusTriggerGroupField> groupFields;
}
