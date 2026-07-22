package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "PermissionCountV1")
public class PermissionCountDTOv1 extends CountDTOv1 {
    @Schema(description = "permission group id", example = DTOExamples.PERMISSION_GROUP_ID)
    @RelatedObject(type = PermissionGroupDTOv1.class, name = "group")
    public UUID groupId;
}
