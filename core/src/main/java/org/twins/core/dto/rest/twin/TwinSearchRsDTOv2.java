package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.PaginationBean;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinSearchRsV2")
public class TwinSearchRsDTOv2 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "pagination data")
    public PaginationBean pagination;

    @Schema(description = "results - transfers list")
    public List<TwinDTOv2> twinList;
}
