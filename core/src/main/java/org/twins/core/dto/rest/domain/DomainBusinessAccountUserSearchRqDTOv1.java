package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountUserSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainBusinessAccountUserSearchRqV1")
public class DomainBusinessAccountUserSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainBusinessAccountUserSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public DomainBusinessAccountUserSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
