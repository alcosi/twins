package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinclass.TwinClassFieldDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;
import org.twins.core.enums.history.HistoryType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "HistoryV1")
public class HistoryDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "twin Id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "twin")
    public UUID twinId;

    @Schema(description = "history batch id")
    public UUID batchId;

    @Schema(description = "actor User", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "actorUser")
    public UUID actorUserId;

    @Schema(description = "machine User", example = DTOExamples.USER_ID)
    @RelatedObject(type = UserDTOv1.class, name = "machineUser")
    public UUID machineUserId;

    @Schema(description = "history type")
    public HistoryType type;

    @Schema(description = "twin class field id", example = DTOExamples.TWIN_CLASS_FIELD_ID)
    @RelatedObject(type = TwinClassFieldDTOv1.class, name = "twinClassField")
    public UUID twinClassFieldId;

    @Schema(description = "created at")
    public LocalDateTime createdAt;

    @Schema(description = "change description")
    String changeDescription;
}
