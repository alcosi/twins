package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinTouchEntity;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinTouchV1")
public class TwinTouchDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "twinId", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "touchId", example = DTOExamples.TWIN_TOUCH)
    public TwinTouchEntity.Touch touchId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "twin")
    public TwinBaseDTOv1 twin;
}
