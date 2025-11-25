package org.twins.core.dto.rest.twinflow;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinflowBaseV1")
public class TwinflowBaseDTOv1 { //todo rename me
    @Schema(example = DTOExamples.TWINFLOW_ID)
    public UUID id;

    @Schema(example = DTOExamples.TWIN_CLASS_ID)
    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    public UUID twinClassId;

    @Schema(description = "name", example = "Project")
    public String name;

    @Schema(description = "description", example = "Projects business objects")
    public String description;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "createdByUserId")
    @RelatedObject(type = UserDTOv1.class, name = "createdByUser")
    public UUID createdByUserId;

    @Schema(description = "initialStatusId")
    @RelatedObject(type = TwinStatusDTOv1.class, name = "initialStatus")
    public UUID initialStatusId;

    @Schema(description = "initialSketchStatusId")
    public UUID initialSketchStatusId;
}
