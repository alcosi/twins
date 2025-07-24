package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassSearchConfiguredRqV1")
public class TwinClassSearchConfiguredRqDTOv1 extends Request {
    @Schema(description = "Search named params values")
    public Map<String, String> params;

    @Schema(description = "search narrow")
    public TwinClassSearchDTOv1 narrow;
}
