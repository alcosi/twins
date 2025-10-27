package org.twins.core.dto.rest.datalist;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "DataListCreateRqV1")
public class DataListCreateRqDTOv1 extends DataListSaveRqDTOv1 {
    @Schema(description = "default option")
    public DataListOptionDefaultCreateDTOv1 defaultOption;
}
