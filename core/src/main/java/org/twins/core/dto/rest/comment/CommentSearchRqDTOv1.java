package org.twins.core.dto.rest.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DataTimeRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode
@Schema(name =  "CommentSearchRqDTOv1")
public class CommentSearchRqDTOv1  {
    @Schema(description = "List of ids to select")
    private Set<UUID> idList;

    @Schema(description = "List of ids to exclude")
    private Set<UUID> idExcludeList;

    @Schema(description = "List of twin ids to select")
    private Set<UUID> twinIdList;

    @Schema(description = "List of twin ids to exclude")
    private Set<UUID> twinIdExcludeList;

    @Schema(description = "List of created by user Id's to select")
    private Set<UUID> createdByUserIdList;

    @Schema(description = "List of created by user Id's to exclude")
    private Set<UUID> createdByUserIdExcludeList;

    @Schema(description = "Full text search list to select")
    private Set<String> textLikeList;

    @Schema(description = "Full text search list to exclude")
    private Set<String> textNotLikeList;

    @Schema(description = "created at")
    public DataTimeRangeDTOv1 createdAt;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "updated at")
    public DataTimeRangeDTOv1 updatedAt;
}
