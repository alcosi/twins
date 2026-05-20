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
@Schema(name = "TwinClassFieldConditionUpdateRqV1")
public class TwinClassFieldConditionUpdateRqDTOv1 extends Request {

    @Schema(description = "conditions")
    public List<TwinClassFieldConditionUpdateDTOv1> conditions;
}
