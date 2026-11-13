package org.twins.core.dto.rest.resource;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.file.FileData;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ResourceUpdateV1")
public class ResourceUpdateDTOv1 {
    @NotNull
    @Schema(description = "resource id", example = DTOExamples.RESOURCE_ID)
    public UUID id;

    @Schema(description = "New resource name (original file name). Null keeps the current value")
    public String originalFileName;

    @Schema(description = "Public flag: resource is available via the public download api (GET /public/resource/{id}/v1). Null keeps the current value")
    public Boolean isPublic;

    @Schema(description = "Replaces the stored file. Use multipart scheme to send file in the same multipart request. Example : multipart://file[0], where file[0] - multipart filed name. Non-multipart link is stored as an external uri. Null keeps the current file", example = DTOExamples.ATTACHMENT_STORAGE_LINK)
    public String storageLink;

    @JsonIgnore
    @Schema(hidden = true)
    public FileData file; // resolved from storageLink against the multipart parts by the controller; never comes from json
}
