package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.ActionRestrictionReasonGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ActionRestrictionReasonCountRqV1")
public class ActionRestrictionReasonCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public ActionRestrictionReasonSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<ActionRestrictionReasonGroupField> groupFields;
}
