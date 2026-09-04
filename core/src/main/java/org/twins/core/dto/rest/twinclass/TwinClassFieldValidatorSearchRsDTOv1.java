package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldValidatorSearchRsV1")
public class TwinClassFieldValidatorSearchRsDTOv1 extends TwinClassFieldValidatorListRsDTOv1 {

    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
