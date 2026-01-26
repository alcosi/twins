package org.twins.core.dto.rest.factory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "FactoryConditionSearchRsV1")
public class FactoryConditionSearchRsDTOv1 extends FactoryConditionListRsDTOv1 {

    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;
}
