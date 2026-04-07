package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(name = "TwinValidatorSearchRsV1")
@Accessors(chain = true)
public class TwinValidatorSearchRsDTOv1 extends TwinValidatorListRsDTOv1 {

    @Schema(description = "pagination")
    public PaginationDTOv1 pagination;
}
