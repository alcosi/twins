package org.twins.core.dto.rest.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.IntegerRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "AttachmentRestrictionSearchV1")
public class AttachmentRestrictionSearchDTOv1 {
    @Schema(description = "attachment restriction id list")
    public Set<UUID> idList;

    @Schema(description = "attachment restriction id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "min count range")
    public IntegerRangeDTOv1 minCountRange;

    @Schema(description = "max count range")
    public IntegerRangeDTOv1 maxCountRange;

    @Schema(description = "file size mb limit range")
    public IntegerRangeDTOv1 fileSizeMbLimitRange;

    @Schema(description = "file extension list like list")
    public Set<String> fileExtensionLimitLikeList;

    @Schema(description = "file extension list not like list")
    public Set<String> fileExtensionLimitNotLikeList;

    @Schema(description = "file name regexp like list")
    public Set<String> fileNameRegexpLikeList;

    @Schema(description = "file name regexp not like list")
    public Set<String> fileNameRegexpNotLikeList;
}
