package org.twins.core.dto.rest.resource;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;
import org.twins.core.dto.rest.LongRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "ResourceSearchV1")
public class ResourceSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "original file name like list")
    public Set<String> originalFileNameLikeList;

    @Schema(description = "original file name not like list")
    public Set<String> originalFileNameNotLikeList;

    @Schema(description = "uploaded by user id list")
    public Set<UUID> uploadedByUserIdList;

    @Schema(description = "uploaded by user id exclude list")
    public Set<UUID> uploadedByUserIdExcludeList;

    @Schema(description = "storage id list")
    public Set<UUID> storageIdList;

    @Schema(description = "storage id exclude list")
    public Set<UUID> storageIdExcludeList;

    @Schema(description = "storage file key like list")
    public Set<String> storageFileKeyLikeList;

    @Schema(description = "storage file key not like list")
    public Set<String> storageFileKeyNotLikeList;

    @Schema(description = "size in bytes")
    public LongRangeDTOv1 sizeInBytes;

    @Schema(description = "created at")
    public DataTimeRangeDTOv1 createdAt;

    @Schema(description = "public flag filter")
    public Ternary isPublic;
}
