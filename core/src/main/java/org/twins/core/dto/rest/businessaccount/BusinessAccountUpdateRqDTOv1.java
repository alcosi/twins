package org.twins.core.dto.rest.businessaccount;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "BusinessAccountUpdateRqV1")
public class BusinessAccountUpdateRqDTOv1 extends Request {
    @Schema(description = "name", example = "BuildmeUp Const.")
    public String name;
}
