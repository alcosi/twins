package org.twins.core.dto.rest.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.draft.DraftEntity;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DraftBaseV1")
public class DraftBaseDTOv1 {
    @Schema(description = "draft id")
    public UUID id;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "created by user")
    public UUID createdByrUserId;

    @Schema(description = "count of new twins")
    public Integer twinCreateCount;

    @Schema(description = "count of updated twins")
    public Integer twinUpdateCount;

    @Schema(description = "count of deleted twins")
    public Integer twinDeleteCount;

    @Schema(description = "draft status")
    public DraftEntity.Status status;
}
