package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinStatusSaveV1")
public class TwinStatusSaveDTOv1 extends TwinStatusDTOv1{

}
