package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinValidatorSetSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorSetSearchRqV1")
public class TwinValidatorSetSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinValidatorSetSearchDTOv1 search;

    @Schema(description = "Sort field. Default: name")
    public TwinValidatorSetSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
