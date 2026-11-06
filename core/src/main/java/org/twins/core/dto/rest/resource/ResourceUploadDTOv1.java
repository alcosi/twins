package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ResourceUploadV1")
public class ResourceUploadDTOv1 {
    @Schema(description = "created resource id, file can be downloaded via GET /public/resource/{resourceId}/v1")
    public UUID resourceId;
    @Schema(description = "file name as it was sent by the client")
    public String originalFileName;
    @Schema(description = "stored file size in bytes")
    public Long sizeInBytes;
    @Schema(description = "direct link to the stored file in the domain storage")
    public String storageLink;
    @Schema(description = "relative download uri served by twins")
    public String downloadUri;
}
