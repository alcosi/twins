package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionMapRsV1")
public class DataListOptionMapRsDTOv1 extends Response {
    @Schema(description = "map { data list option id / data list option }")
    public Map<UUID, DataListOptionDTOv1> dataListOptionMap;
}
