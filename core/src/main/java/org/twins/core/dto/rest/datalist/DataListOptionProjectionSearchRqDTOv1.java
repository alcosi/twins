package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "DataListOptionProjectionSearchRqV1")
public class DataListOptionProjectionSearchRqDTOv1 extends Request {
    @Schema
    public DataListOptionProjectionSearchDTOv1 search;
}
