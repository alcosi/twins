package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ResourceSearchRsV1")
public class ResourceSearchRsDTOv1 extends ResourceListRsDTOv1 {

    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

}
