package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "public class TwinSearchIncludeByLinkV1")
public class TwinSearchIncludeByLinkDTOv1 {
    @Schema(description = "Twin class id list")
    public UUID linkId;

    @Schema(description = "Head twin id list")
    public List<UUID> dstTwinIdList;
}


