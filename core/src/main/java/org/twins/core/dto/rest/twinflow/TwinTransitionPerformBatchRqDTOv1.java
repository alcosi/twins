package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformBatchRqV1")
public class TwinTransitionPerformBatchRqDTOv1 extends Request {
    @Schema
    public List<UUID> twinIdList;

    @Schema
    public String batchComment;

    @Schema(description = "Data to be update in all target twins during transition (if allowEdit = true)")
    public TwinUpdateDTOv1 batchUpdate;
}
