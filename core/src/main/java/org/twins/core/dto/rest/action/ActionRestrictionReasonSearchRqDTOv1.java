package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ActionRestrictionReasonSearchRqV1")
public class ActionRestrictionReasonSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public ActionRestrictionReasonSearchDTOv1 search;
}
