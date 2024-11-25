package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name =  "AttachmentQuotasBaseV1")
public class AttachmentQuotasBaseDTOv1 {

    @Schema(description = "Quota count")
    public Long quotaCount;

    @Schema(description = "Quota size")
    public Long quotaSize;

    @Schema(description = "Used count")
    public Long usedCount;

    @Schema(description = "Used size")
    public Long usedSize;
}
