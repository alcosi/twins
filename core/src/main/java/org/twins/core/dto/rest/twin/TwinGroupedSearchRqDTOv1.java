package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinGroupedSearchRqV1")
public class TwinGroupedSearchRqDTOv1 extends Request {
    @Schema(description = "Twin filter of exactly one class. Sort does not belong here")
    public TwinSearchDTOv1 search;

    @NotNull
    @Valid
    @Size(min = 1, max = 3)
    @Schema(description = "Grouping levels, top to bottom. Each level reads its key from the leaf")
    public List<TwinGroupCriterionDTOv1> criteria;

    @Valid
    @Size(max = 2)
    @Schema(description = "Order of twins inside the last level. Empty means createdAt DESC, then id ASC. Basic fields only")
    public List<TwinSortDTOv1> memberSorts;
}
