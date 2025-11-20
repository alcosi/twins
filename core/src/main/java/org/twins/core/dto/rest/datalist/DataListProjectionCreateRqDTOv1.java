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
@Schema(name = "DataListProjectionCreateRqV1")
public class DataListProjectionCreateRqDTOv1 extends Request {
    @Schema(description = "data list projection list")
    public List<DataListProjectionCreateDTOv1> dataListProjectionList;
}
