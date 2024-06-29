package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.mappers.rest.MapperMode;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStarredV1")
@MapperModeBinding(modes = MapperMode.StarredMode.class)
public class TwinStarredDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "twinId", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @MapperModeBinding(modes = MapperMode.StarredTwinMode.class)
    @Schema(description = "twin")
    public TwinBaseDTOv1 twin;
}
