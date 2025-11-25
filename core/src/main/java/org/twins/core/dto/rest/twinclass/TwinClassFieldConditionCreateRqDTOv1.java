package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldConditionCreateRqV1")
public class TwinClassFieldConditionCreateRqDTOv1 extends Request {
    @Schema(description = "conditions")
    public List<TwinClassFieldConditionCreateDTOv1> conditions;
}
