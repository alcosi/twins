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
@Schema(name =  "TwinSearchByAliasRqV1")
public class TwinSearchByAliasRqDTOv1 extends Request {
    @Schema(description = "Search named params values")
    public Map<String, String> params;

    @Schema(description = "search narrowing")
    public TwinSearchWithHeadDTOv1 narrow;
}
