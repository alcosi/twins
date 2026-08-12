package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinFactoryTriggerGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinFactoryTriggerCountRqV1")
public class FactoryTriggerCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public FactoryTriggerSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<TwinFactoryTriggerGroupField> groupFields;
}
