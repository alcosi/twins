package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionProjectionUpdateRqV1")
public class DataListOptionProjectionUpdateRqDTOv1 extends Request {
    @Schema(description = "data list option projections")
    public List<DataListOptionProjectionUpdateDTOv1> dataListOptionProjections;
}
