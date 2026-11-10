package org.twins.core.dto.rest.resource;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ResourceV1")
public class ResourceDTOv1 {
    @Schema(description = "resource id, file can be downloaded via GET /public/resource/{id}/v1", example = DTOExamples.UUID_ID)
    public UUID id;

    @Schema(description = "uploader user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "uploadedByUser")
    public UUID uploadedByUserId;

    @Schema(description = "storage id", example = DTOExamples.RESOURCE_STORAGE_ID)
    @RelatedObject(type = StorageDTOv1.class, name = "storage")
    public UUID storageId;

    @Schema(description = "original file name")
    public String originalFileName;

    @Schema(description = "file size in bytes")
    public Long sizeInBytes;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "upload timestamp", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "direct link to the stored file in the domain storage")
    public String storageLink;
}
