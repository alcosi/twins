package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "ActionRestrictionReasonCountV1")
public class ActionRestrictionReasonCountDTOv1 extends CountDTOv1 {
    @Schema(description = "action restriction reason type")
    public String type;
}
