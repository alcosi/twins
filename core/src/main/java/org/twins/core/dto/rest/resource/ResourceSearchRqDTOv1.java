package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.ResourceSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ResourceSearchRqV1")
public class ResourceSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public ResourceSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public ResourceSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
