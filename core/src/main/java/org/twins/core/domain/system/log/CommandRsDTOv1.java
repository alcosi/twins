package org.twins.core.domain.system.log;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "CommandRsV1")
public class CommandRsDTOv1 extends Response {
    @Schema(description = "Command for run script")
    public String command;
}
