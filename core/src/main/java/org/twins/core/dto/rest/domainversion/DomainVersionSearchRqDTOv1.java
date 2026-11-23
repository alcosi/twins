package org.twins.core.dto.rest.domainversion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.DomainVersionSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DomainVersionSearchRqV1")
public class DomainVersionSearchRqDTOv1 extends Request {
    @Schema(description = "search params")
    public DomainVersionSearchDTOv1 search;

    @Schema(description = "Sort field. Default: startedAt")
    public DomainVersionSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
