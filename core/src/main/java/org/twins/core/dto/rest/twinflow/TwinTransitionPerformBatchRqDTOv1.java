package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.twin.TwinUpdateDTOv1;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinTransitionPerformBatchRqV1")
public class TwinTransitionPerformBatchRqDTOv1 extends TwinTransitionDraftBatchRqDTOv1 {
    @Schema
    public String batchComment;

    @Schema(description = "some extra data to perform transition")
    public TwinTransitionContextDTOv1 batchContext;
}
