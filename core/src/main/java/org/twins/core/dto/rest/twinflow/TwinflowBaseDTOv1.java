package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinclass.TwinClassBaseDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowBaseV1")
public class TwinflowBaseDTOv1 {
    @Schema(example = DTOExamples.TWINFLOW_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;

    @Schema(description = "name", example = "Project")
    public String name;

    @Schema(description = "description", example = "Projects business objects")
    public String description;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "createdByUserId")
    public UUID createdByUserId;

    @Schema(description = "initialStatusId")
    public UUID initialStatusId;

    @Schema(description = "initialSketchStatusId")
    public UUID initialSketchStatusId;

    @Schema(description = "twin class")
    public TwinClassBaseDTOv1 twinClass;

}
