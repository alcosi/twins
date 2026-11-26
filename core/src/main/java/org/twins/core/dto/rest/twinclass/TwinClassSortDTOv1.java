package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.SortDTOv1;
import org.twins.core.enums.sort.TwinClassSortField;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassSortV1")
public class TwinClassSortDTOv1 extends SortDTOv1 {
    @NotNull
    @Schema(description = "Sort field. Default: key")
    public TwinClassSortField sortField;
}
