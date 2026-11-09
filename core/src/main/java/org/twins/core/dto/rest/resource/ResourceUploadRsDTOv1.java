package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "ResourceUploadRsV1")
public class ResourceUploadRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "uploaded files list, one entry per received file part")
    public List<ResourceDTOv1> files;
}
