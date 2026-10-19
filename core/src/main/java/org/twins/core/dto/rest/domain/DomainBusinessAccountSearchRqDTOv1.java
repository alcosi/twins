package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainBusinessAccountSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "DomainBusinessAccountSearchRqV1")
public class DomainBusinessAccountSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainBusinessAccountSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public DomainBusinessAccountSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
