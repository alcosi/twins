package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Deprecated
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "DataListOptionCreateRqV1")
public class DataListOptionCreateRqDTOv1 extends Request {
    @Schema(description = "data list option create")
    public DataListOptionCreateDTOv1 dataListOptionCreate;
}
