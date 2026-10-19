package org.twins.core.dto.rest.featurer;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.FeaturerTypeSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "FeaturerTypeSearchRqV1")
public class FeaturerTypeSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public FeaturerTypeSearchDTOv1 search;

    @Schema(description = "Sort field. Default: name")
    public FeaturerTypeSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
