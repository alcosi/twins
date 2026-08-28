package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "PermissionSearchV1")
public class PermissionSearchDTOv1 {
    @Size(max = 50)
    @Schema(description = "id list")
    public Set<UUID> idList;
    @Size(max = 50)
    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;
    @Size(max = 50)
    @Schema(description = "key like list")
    public Set<String> keyLikeList;
    @Size(max = 50)
    @Schema(description = "key not like list")
    public Set<String> keyNotLikeList;
    @Size(max = 50)
    @Schema(description = "name like list")
    public Set<String> nameLikeList;
    @Size(max = 50)
    @Schema(description = "name not like list")
    public Set<String> nameNotLikeList;
    @Size(max = 50)
    @Schema(description = "description like list")
    public Set<String> descriptionLikeList;
    @Size(max = 50)
    @Schema(description = "description not like list")
    public Set<String> descriptionNotLikeList;
    @Size(max = 50)
    @Schema(description = "group id list")
    public Set<UUID> groupIdList;
    @Size(max = 50)
    @Schema(description = "group id exclude list")
    public Set<UUID> groupIdExcludeList;
}
