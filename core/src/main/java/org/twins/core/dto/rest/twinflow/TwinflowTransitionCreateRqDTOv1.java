package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowTransitionCreateRqV1")
public class TwinflowTransitionCreateRqDTOv1 extends Request {

    @Schema(description = "[optional] src status id. if null - from any status transition", example = DTOExamples.TWIN_STATUS_ID)
    public UUID srcStatusId;

    @Schema(description = "dst status is required", example = DTOExamples.TWIN_STATUS_ID)
    public UUID dstStatusId;

    @Schema(description = "[optional] name", example = DTOExamples.TWIN_STATUS_NAME)
    public String name;

    @Schema(description = "[optional] uniq alias inside twinflow", example = DTOExamples.TWINFLOW_TRANSITION_ALIAS)
    public String alias;

    @Schema(description = "[optional] some permission required to run transition", example = DTOExamples.PERMISSION_ID)
    public UUID permissionId;

    @JsonIgnore
    public UUID twinflowId;

}
