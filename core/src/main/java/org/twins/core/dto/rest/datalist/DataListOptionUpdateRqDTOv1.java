package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionUpdateRqV1")
public class DataListOptionUpdateRqDTOv1 extends Request {
    @Schema(description = "data list option update")
    public DataListOptionUpdateDTOv1 dataListOptionUpdate;
}
