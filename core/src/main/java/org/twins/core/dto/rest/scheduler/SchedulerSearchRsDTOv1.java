package org.twins.core.dto.rest.scheduler;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;
import org.twins.core.dto.rest.pagination.PaginationDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "SchedulerSearchRsV1")
public class SchedulerSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "pagination data")
    public PaginationDTOv1 pagination;

    @Schema(description = "results - scheduler list")
    public List<SchedulerDTOv1> schedulers;
}
