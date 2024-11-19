package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionGroupSearchRqV1")
public class PermissionGroupSearchRqDTOv1 extends Request {
    @Schema(description = "id list", example = DTOExamples.PERMISSION_GROUP_ID)
    public Set<UUID> idList;

    @Schema(description = "id exclude list", example = DTOExamples.PERMISSION_GROUP_ID)
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class id exclude list", example = DTOExamples.TWIN_CLASS_ID)
    public Set<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list", example = DTOExamples.TWIN_CLASS_ID)
    public Set<UUID> twinClassIdExcludeList;

    @Schema(description = "key like list", example = DTOExamples.PERMISSION_GROUP_KEY)
    public Set<String> keyLikeList;

    @Schema(description = "key not like list", example = DTOExamples.PERMISSION_GROUP_KEY)
    public Set<String> keyNotLikeList;

    @Schema(description = "name like list", example = DTOExamples.NAME)
    public Set<String> nameLikeList;

    @Schema(description = "name not like list", example = DTOExamples.NAME)
    public Set<String> nameNotLikeList;

    @Schema(description = "description like list", example = DTOExamples.DESCRIPTION)
    public Set<String> descriptionLikeList;

    @Schema(description = "description not like list", example = DTOExamples.DESCRIPTION)
    public Set<String> descriptionNotLikeList;

    @Schema(description = "show system group", example = DTOExamples.BOOLEAN_TRUE)
    public boolean showSystemGroups;
}
