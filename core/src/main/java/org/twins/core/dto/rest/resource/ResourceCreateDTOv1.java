package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

@Data
@Accessors(chain = true)
@Schema(name = "ResourceCreateV1")
public class ResourceCreateDTOv1 {
    @Schema(description = "External storage link. Use multipart scheme to send file in the same multipart request. Example : multipart://file[0], where file[0] - multipart filed name. Non-multipart link is stored as an external uri", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String storageLink;

    @Schema(description = "Optional resource name override, defaults to the uploaded file name", example = DTOExamples.ATTACHMENT_TITLE)
    public String title;
}
