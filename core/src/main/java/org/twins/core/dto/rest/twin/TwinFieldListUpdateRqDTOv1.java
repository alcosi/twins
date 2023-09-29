package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinFieldListUpdateRqV1")
public class TwinFieldListUpdateRqDTOv1 extends Request {
    @Schema(description = "fields")
    public Map<String, String> fields;
}
