package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinStatusTriggerCreateRqV1")
public class TwinStatusTriggerCreateRqDTOv1 extends Request {
    @Schema(description = "twin status triggers")
    public List<TwinStatusTriggerCreateDTOv1> twinStatusTriggers;
}
