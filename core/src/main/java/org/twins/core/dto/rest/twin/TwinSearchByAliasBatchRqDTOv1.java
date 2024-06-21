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
@Schema(name =  "TwinSearchByAliasBatchRqV1")
public class TwinSearchByAliasBatchRqDTOv1 extends Request {
    @Schema(description = "Search alias ref alias request body(TwinSearchByAliasRqV1)")
    public Map<String, TwinSearchByAliasRqDTOv1> searchMap;
}
