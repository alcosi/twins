package org.twins.core.dto.rest.action;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ActionRestrictionReasonSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ActionRestrictionReasonSearchRqV1")
public class ActionRestrictionReasonSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public ActionRestrictionReasonSearchDTOv1 search;

    @Schema(description = "Sort field. Default: type")
    public ActionRestrictionReasonSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
