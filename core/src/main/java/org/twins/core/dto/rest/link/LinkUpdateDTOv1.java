package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.common.BasicUpdateOperationDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "LinkUpdateV1")
public class LinkUpdateDTOv1 extends LinkSaveDTOv1 {

    @Schema(description = "[optional] should be filled on change source twin class id of link")
    public BasicUpdateOperationDTOv1 srcTwinClassUpdate;

    @Schema(description = "[optional] should be filled on change destination twin class id of link")
    public BasicUpdateOperationDTOv1 dstTwinClassUpdate;

}
