package org.twins.core.dto.rest.transition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TransitionAliasSearchRsV1")
public class TransitionAliasSearchRsDTOv1 extends Response {
    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "transition alias list")
    private List<TransitionAliasDTOv1> aliasList;
}
