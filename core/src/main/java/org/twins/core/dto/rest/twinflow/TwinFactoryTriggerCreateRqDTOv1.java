package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(name = "TwinFactoryTriggerCreateRqV1")
public class TwinFactoryTriggerCreateRqDTOv1 extends Request {
    @Schema(description = "twin factory triggers")
    public List<TwinFactoryTriggerCreateDTOv1> twinFactoryTriggers;
}
