package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "ResourceCountV1")
public class ResourceCountDTOv1 extends CountDTOv1 {
    @Schema(description = "uploader user id", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "uploadedByUser")
    public UUID uploadedByUserId;

    @Schema(description = "storage id", example = DTOExamples.RESOURCE_STORAGE_ID)
    @RelatedObject(type = StorageDTOv1.class, name = "storage")
    public UUID storageId;

    @Schema(description = "public flag: resource is available via the public download api (GET /public/resource/{id}/v1)")
    public Boolean isPublic;
}
