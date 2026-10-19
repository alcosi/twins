package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.SortDirection;
import org.twins.core.enums.sort.TwinClassFieldValidatorSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldValidatorSearchRqV1")
public class TwinClassFieldValidatorSearchRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinClassFieldValidatorSearchDTOv1 search;

    @Schema(description = "Sort field. Default: twinClassFieldId")
    public TwinClassFieldValidatorSortField sortField;

    @Schema(description = "Sort direction: ASC or DESC. Default: ASC")
    public SortDirection sortDirection;
}
