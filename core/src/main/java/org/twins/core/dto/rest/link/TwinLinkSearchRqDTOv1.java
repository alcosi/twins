package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinLinkSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinLinkSearchRqV1")
public class TwinLinkSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinLinkSearchDTOv1 search;

    @Schema(description = "Sort field. Default: createdAt")
    public TwinLinkSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
