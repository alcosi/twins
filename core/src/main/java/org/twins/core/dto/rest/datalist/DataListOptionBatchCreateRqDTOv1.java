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
@Schema(name = "DataListOptionBatchCreateRqV1")
public class DataListOptionBatchCreateRqDTOv1 extends Request {
    @Schema(description = "data list options")
    public List<DataListOptionCreateDTOv1> dataListOptions;
}
