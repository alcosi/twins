package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ResourceSearchV1")
public class ResourceSearchDTOv1 {
    @Schema(description = "resource id list")
    public Set<UUID> idList;
    @Schema(description = "original file name contains (case insensitive)")
    public Set<String> originalFileNameLikeList;
    @Schema(description = "uploaded by user id list")
    public Set<UUID> uploadedByUserIdList;
}
