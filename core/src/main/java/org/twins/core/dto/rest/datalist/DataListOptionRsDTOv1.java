package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListOptionRsV1")
public class DataListOptionRsDTOv1 extends Response {
    @Schema(description = "data lists option")
    public DataListOptionDTOv2 option;
}
