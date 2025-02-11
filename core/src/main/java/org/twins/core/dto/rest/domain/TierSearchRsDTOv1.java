package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TagSearchRsDTOv1")
public class TierSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "pagination")
    private PaginationDTOv1 pagination;

    @Schema(description = "comments")
    private List<TierDTOv1> comments;
}
