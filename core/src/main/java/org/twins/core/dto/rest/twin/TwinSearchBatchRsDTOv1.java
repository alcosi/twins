package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.Map;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinSearchBatchRsV1")
public class TwinSearchBatchRsDTOv1 extends Response {
    @Schema(description = "Map { frontendId / count }")
    public Map<String, Long> response;

}
