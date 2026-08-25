package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.SpaceRoleSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SpaceRoleSearchRqV1")
public class SpaceRoleSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public SpaceRoleSearchDTOv1 search;

    @Schema(description = "Sort field. Default: key")
    public SpaceRoleSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
