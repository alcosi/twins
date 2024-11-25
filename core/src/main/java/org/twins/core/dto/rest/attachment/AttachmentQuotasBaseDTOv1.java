package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;


@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "AttachmentQuotasBaseV1")
public class AttachmentQuotasBaseDTOv1 extends Request {

    @Schema(description = "Quota count")
    public Long quotaCount;

    @Schema(description = "Quota size")
    public Long quotaSize;

    @Schema(description = "Used count")
    public Long usedCount;

    @Schema(description = "Used size")
    public Long usedSize;
}
