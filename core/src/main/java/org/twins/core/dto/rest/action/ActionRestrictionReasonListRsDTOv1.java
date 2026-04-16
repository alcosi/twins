package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@EqualsAndHashCode(callSuper = false)
@Data
@Accessors(chain = true)
@Schema(name = "ActionRestrictionReasonListRsV1")
public class ActionRestrictionReasonListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "action restriction reason list")
    public List<ActionRestrictionReasonDTOv1> actionRestrictionReasons;
}
