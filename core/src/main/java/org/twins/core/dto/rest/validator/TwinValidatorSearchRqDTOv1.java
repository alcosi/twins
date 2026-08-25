package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinValidatorSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorSearchRqV1")
public class TwinValidatorSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinValidatorSearchDTOv1 search;

    @Schema(description = "Sort field. Default: order")
    public TwinValidatorSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
