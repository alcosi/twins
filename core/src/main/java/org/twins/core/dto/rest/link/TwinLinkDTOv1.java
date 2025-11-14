package org.twins.core.dto.rest.link;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinLinkV1")
public class TwinLinkDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_LINK_ID)
    public UUID id;

    @Schema(description = "src twin id", example = DTOExamples.TWIN_ID)
    public UUID srcTwinId;

    @Schema(description = "dst twin id", example = DTOExamples.TWIN_ID)
    public UUID dstTwinId;

    @Schema(description = "link id", example = DTOExamples.LINK_ID)
    public UUID linkId;

    @Schema(description = "created by user id", example = DTOExamples.USER_ID)
    public UUID createdByUserId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;
}
