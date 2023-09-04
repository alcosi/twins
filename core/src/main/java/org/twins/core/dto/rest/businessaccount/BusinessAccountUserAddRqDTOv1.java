package org.twins.core.dto.rest.businessaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "BusinessAccountUserAddRqV1")
public class BusinessAccountUserAddRqDTOv1 extends Request {
    @Schema(description = "id", example = "c2a7f81f-d7da-43e8-a1d3-18d6f632878b")
    public UUID userId;
}
