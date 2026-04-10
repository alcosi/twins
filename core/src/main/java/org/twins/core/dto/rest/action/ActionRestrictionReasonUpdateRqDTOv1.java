package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ActionRestrictionReasonUpdateRqV1")
public class ActionRestrictionReasonUpdateRqDTOv1 extends Request {
    @Schema(description = "action restriction reason list")
    public List<ActionRestrictionReasonUpdateDTOv1> actionRestrictionReasons;
}
