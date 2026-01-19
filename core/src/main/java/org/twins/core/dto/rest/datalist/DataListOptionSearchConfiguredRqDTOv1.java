package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(name = "DataListOptionSearchConfiguredRqV1")
public class DataListOptionSearchConfiguredRqDTOv1 {
    @Schema(description = "Search named params values")
    public Map<String, String> params;

    @Schema(description = "search narrow")
    public DataListOptionSearchDTOv1 narrow;
}
