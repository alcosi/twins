package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ActionRestrictionReasonCreateV1")
public class ActionRestrictionReasonCreateDTOv1 extends ActionRestrictionReasonSaveDTOv1 {
}
