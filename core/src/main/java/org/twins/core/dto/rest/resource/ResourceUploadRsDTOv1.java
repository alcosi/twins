package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "ResourceUploadRsV1")
public class ResourceUploadRsDTOv1 extends Response {
    @Schema(description = "uploaded files list, one entry per received file part")
    public List<ResourceUploadDTOv1> files;
}
