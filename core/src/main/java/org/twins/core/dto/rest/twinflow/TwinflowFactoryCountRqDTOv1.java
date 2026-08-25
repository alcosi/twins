package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinflowFactoryGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinflowFactoryCountRqV1")
public class TwinflowFactoryCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinflowFactorySearchDTOv1 search;

    @Size(max = 3)
    @Schema(description = "Group by fields")
    public Set<TwinflowFactoryGroupField> groupFields;
}
