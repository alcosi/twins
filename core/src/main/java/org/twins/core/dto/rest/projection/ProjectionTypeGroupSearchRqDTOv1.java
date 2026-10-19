package org.twins.core.dto.rest.projection;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ProjectionTypeGroupSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ProjectionTypeGroupSearchRqV1")
public class ProjectionTypeGroupSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public ProjectionTypeGroupSearchDTOv1 search;

    @Schema(description = "Sort field. Default: key")
    public ProjectionTypeGroupSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
